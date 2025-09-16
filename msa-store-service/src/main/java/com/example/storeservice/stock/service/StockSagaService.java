package com.example.storeservice.stock.service;

import com.example.storeservice.entity.MenuInventory;
import com.example.storeservice.repository.MenuInventoryRepository;
import com.example.storeservice.stock.KafkaJsonProducer;
import com.example.storeservice.stock.model.*;
import com.example.storeservice.stock.model.Enums.*;
import com.example.storeservice.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.storeservice.stock.StockTypes.*;

/**
 * 흐름 시나리오
 * - order.created : 실제 재고 확인 없이 예약(+reserved, +order ledger) → stock.reservation.result
 * - payment PENDING->PAID : finalize(검증+차감) + DB write-through, 실패 시 보상/해제 → stock.finalized or stock.shortage
 * - order PENDING->CANCELLED & payment=PAID : Redis/DB 복구 → stock.restored
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockSagaService {

    private final StockRedisService redis;
    private final KafkaJsonProducer producer;
    private final StockWriteThroughService writeThrough;
    private final MenuInventoryRepository menuInventoryRepository;

    // ---------- Inbound 핸들러 ----------
    /** 1) 주문 생성: 재고 확인 없이 "예약만" 적립 */
    public void onOrderCreated(OrderCreatedEvent evt) {
        if (!redis.markProcessed(evt.getEventId())) {
            log.info("[IDEMPOTENT] order.created already processed eventId={}", evt.getEventId());
            return;
        }
        log.info("[SAGA][ORDER.CREATED] orderId={} items{}",
                evt.getOrderId(),
                evt.getItems().stream().map(i -> i.getMenuId()+":"+i.getQty()).collect(Collectors.joining(",")));

        boolean allOk = true; // 항상 true가 예상됨
        for (OrderItem it : evt.getItems()) {
            if (!redis.reserve(evt.getOrderId(), it.getMenuId(), it.getQty())) {
                allOk = false;
                break;
            }
        }

        StockReservationResultEvent out = StockReservationResultEvent.builder()
                .type(STOCK_RESERVATION_RESULT)
                .eventId(UUID.randomUUID())
                .orderId(evt.getOrderId())
                .storeId(evt.getStoreId())
                .occurredAt(Instant.now())
                .customerId(evt.getCustomerId())
                .ownerId(evt.getOwnerId())
                .items(evt.getItems())
                .success(allOk)
                .reason(allOk ? null : "reserve failed (unexpected)")
                .build();

        producer.sendToStock(out, evt.getOrderId().toString(), out.getType());
        log.info("[SAGA][ORDER.CREATED][DONE] orderId={} success={}", evt.getOrderId(), allOk);
    }

    /** 2) 결제 상태 변경 */
    public void onPaymentChanged(PaymentStatusChangedEvent evt) {
        if (!redis.markProcessed(evt.getEventId())) {
            log.info("[SAGA][SKIP] payment.changed already processed eventId={}", evt.getEventId());
            return;
        }
        log.info("[SAGA][PAYMENT.CHANGED] orderId={} {}->{}", evt.getOrderId(), evt.getFromStatus(), evt.getToStatus());

        if (evt.getFromStatus() == PaymentStatus.PENDING && evt.getToStatus() == PaymentStatus.PAID) {
            boolean allOk = true;
            List<OrderItem> succeeded = new ArrayList<>();

            for (OrderItem it : evt.getItems()) {
                UUID menuId = it.getMenuId();
                int qty = it.getQty();

                // 1) Redis finalize(실재고/예약 검증 + 원자 차감)
                boolean ok = redis.finalizeItem(evt.getOrderId(), menuId, qty);
                if (!ok) { allOk = false; break; }

                // 2) DB write-through (무한 재고는 DB 차감 생략)
                try {
                    MenuInventory inv = menuInventoryRepository.findById(menuId)
                            .orElseThrow(() -> new IllegalStateException("Inventory not found: " + menuId));
                    if (!inv.isInfiniteStock()) writeThrough.finalizeDecrease(menuId, qty);
                    succeeded.add(it);
                } catch (Exception e) {
                    log.error("[SAGA][FINALIZE][DB-FAIL] orderId={} menuId={} qty={} err={}",
                            evt.getOrderId(), menuId, qty, e.toString());
                    allOk = false; break;
                }
            }

            if (allOk) {
                StockFinalizedEvent out = StockFinalizedEvent.builder()
                        .type(STOCK_FINALIZED)
                        .eventId(UUID.randomUUID())
                        .orderId(evt.getOrderId())
                        .storeId(evt.getStoreId())
                        .occurredAt(Instant.now())
                        .customerId(evt.getCustomerId())
                        .ownerId(evt.getOwnerId())
                        .items(evt.getItems())
                        .build();
                producer.sendToStock(out, evt.getOrderId().toString(), out.getType());
                log.info("[SAGA][FINALIZE][OK] orderId={}", evt.getOrderId());
            } else {
                // 보상 + 전체 예약 해제 + shortage 발행
                for (OrderItem it : succeeded) {
                    try {
                        redis.compensateFinalize(evt.getOrderId(), it.getMenuId(), it.getQty());
                        // DB는 finalizeDecrease가 성공했던 품목만 복원 시도(보수적으로 모두 시도)
                        MenuInventory inv = menuInventoryRepository.findById(it.getMenuId()).orElse(null);
                        if (inv != null && !inv.isInfiniteStock()) writeThrough.restoreIncrease(it.getMenuId(), it.getQty());
                    } catch (Exception e) {
                        log.error("[SAGA][COMPENSATE][ERR] orderId={} menuId={} qty={} err={}",
                                evt.getOrderId(), it.getMenuId(), it.getQty(), e.toString());
                    }
                }
                for (OrderItem it : evt.getItems()) {
                    redis.release(evt.getOrderId(), it.getMenuId(), it.getQty());
                }

                StockShortageEvent shortage = StockShortageEvent.builder()
                        .type(STOCK_SHORTAGE)
                        .eventId(UUID.randomUUID())
                        .orderId(evt.getOrderId())
                        .storeId(evt.getStoreId())
                        .occurredAt(Instant.now())
                        .customerId(evt.getCustomerId())
                        .ownerId(evt.getOwnerId())
                        .items(evt.getItems())
                        .message("shortage at finalize (paid)")
                        .build();
                producer.sendToStock(shortage, evt.getOrderId().toString(), shortage.getType());
                log.info("[SAGA][FINALIZE][SHORTAGE] orderId={}", evt.getOrderId());
            }
        }

        if (evt.getFromStatus() == PaymentStatus.PENDING && evt.getToStatus() == PaymentStatus.FAILED) {
            // 예약 유지(재시도 고려). 로그만 남김.
            log.info("[SAGA][PAYMENT.FAILED] orderId={} keep reservations", evt.getOrderId());
        }
    }

    /** 3) 주문 상태 변경 */
    public void onOrderStatusChanged(OrderStatusChangedEvent evt) {
        if (!redis.markProcessed(evt.getEventId())) {
            log.info("[SAGA][SKIP] order.status.changed already processed eventId={}", evt.getEventId());
            return;
        }
        log.info("[SAGA][ORDER.STATUS] orderId={} {}->{} (payment={})",
                evt.getOrderId(), evt.getFromStatus(), evt.getToStatus(), evt.getPaymentStatus());

        if (evt.getFromStatus() == OrderStatus.PENDING && evt.getToStatus() == OrderStatus.CONFIRMED) {
            log.debug("[SAGA][ORDER.CONFIRMED] orderId={} (no-op)", evt.getOrderId());
        }

        if (evt.getFromStatus() == OrderStatus.PENDING && evt.getToStatus() == OrderStatus.CANCELED) {
            if (evt.getPaymentStatus() == PaymentStatus.PAID) {
                for (OrderItem it : evt.getItems()) {
                    // Redis actual 복구 + DB 복구
                    redis.incrementActual(it.getMenuId(), it.getQty());
                    try { writeThrough.restoreIncrease(it.getMenuId(), it.getQty()); }
                    catch (Exception e) {
                        log.error("[SAGA][RESTORE][DB-FAIL] orderId={} menuId={} qty={} err={}",
                                evt.getOrderId(), it.getMenuId(), it.getQty(), e.toString());
                    }
                }
                StockFinalizedEvent restored = StockFinalizedEvent.builder()
                        .type(STOCK_RESTORED)
                        .eventId(UUID.randomUUID())
                        .orderId(evt.getOrderId())
                        .storeId(evt.getStoreId())
                        .occurredAt(Instant.now())
                        .customerId(evt.getCustomerId())
                        .ownerId(evt.getOwnerId())
                        .items(evt.getItems())
                        .build();
                producer.sendToStock(restored, evt.getOrderId().toString(), restored.getType());
                log.info("[SAGA][RESTORED] orderId={}", evt.getOrderId());
            }
        }
    }
}