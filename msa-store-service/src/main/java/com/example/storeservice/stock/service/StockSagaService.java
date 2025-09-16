package com.example.storeservice.stock.service;

import com.example.storeservice.entity.MenuInventory;
import com.example.storeservice.repository.MenuInventoryRepository;
import com.example.storeservice.stock.KafkaJsonProducer;
import com.example.storeservice.stock.ReservationNotReadyException;
import com.example.storeservice.stock.StockEventHeaders;
import com.example.storeservice.stock.StockTypes;
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
    private final MenuInventoryRepository invRepo;

    // ---------- Inbound 핸들러 ----------
    /** 1) 주문 생성: 재고 확인 없이 "예약만" 적립 */
    public void onOrderCreated(OrderCreatedEvent evt) {
        if (!redis.markProcessed(evt.getEventId())) {
            log.info("[SAGA][SKIP] order.created dup eventId={}", evt.getEventId());
            return;
        }
        log.info("[SAGA][ORDER.CREATED][IN ] orderId={} itemsCnt={} items={}",
                evt.getOrderId(),
                evt.getItems().size(),
                evt.getItems().stream().map(i -> i.getMenuId()+":"+i.getQty()).collect(Collectors.joining(",")));

        boolean allOk = true; // 항상 true가 예상됨
        for (OrderItem it : evt.getItems()) {
            if (!redis.reserve(evt.getOrderId(), it.getMenuId(), it.getQty())) {
                allOk = false; break;
            }
        }

        var out = StockReservationResultEvent.builder()
                .eventId(UUID.randomUUID())
                .orderId(evt.getOrderId())
                .occurredAt(Instant.now())
                .customerId(evt.getCustomerId())
                .items(evt.getItems())
                .success(allOk)
                .reason(allOk ? null : "reserve failed (unexpected)")
                .build();

        producer.sendToStock(out, evt.getOrderId().toString(), STOCK_RESERVATION_RESULT, null);
        log.info("[SAGA][ORDER.CREATED][OUT] orderId={} success={} itemsCnt={}", evt.getOrderId(), allOk, evt.getItems().size());
    }

    /** 2) 결제 상태 변경 */
    public void onPaymentChanged(PaymentStatusChangedEvent evt) {
        if (!redis.markProcessed(evt.getEventId())) {
            log.info("[SAGA][SKIP] payment.changed dup eventId={}", evt.getEventId());
            return;
        }
        log.info("[SAGA][PAYMENT.CHANGED] orderId={} status={}", evt.getOrderId(), evt.getStatus());

        if (evt.getStatus() == PaymentStatus.PAID) {
            // 예약 장부에서 items 복원
            List<OrderItem> items = redis.getOrderItems(evt.getOrderId());
            if (items.isEmpty()) {
                log.warn("[SAGA][PAID][EMPTY-LEDGER] orderId={} -> retry", evt.getOrderId());
                throw new ReservationNotReadyException("reservation ledger empty (order.created not processed yet)");
            }
            boolean allOk = true;
            List<OrderItem> succeeded = new ArrayList<>();

            for (OrderItem it : items) {
                // 1) Redis finalize(실재고/예약 검증 + 원자 차감)
                if (!redis.finalizeItem(evt.getOrderId(), it.getMenuId(), it.getQty())) {
                    allOk = false; break;
                }
                // 2) DB write-through (무한 재고는 DB 차감 생략)
                try {
                    MenuInventory inv = invRepo.findById(it.getMenuId())
                            .orElseThrow(() -> new IllegalStateException("Inventory not found: " + it.getMenuId()));
                    if (!inv.isInfiniteStock()) writeThrough.finalizeDecrease(it.getMenuId(), it.getQty());
                    succeeded.add(it);
                } catch (Exception e) {
                    log.error("[SAGA][FINALIZE][DB-FAIL] orderId={} it={} err={}",
                            evt.getOrderId(), it, e.toString());
                    allOk = false; break;
                }
            }

            if (allOk) {
                var out = StockFinalizedEvent.builder()
                        .eventId(UUID.randomUUID())
                        .orderId(evt.getOrderId())
                        .occurredAt(Instant.now())
                        .items(items)
                        .build();
                producer.sendToStock(out, evt.getOrderId().toString(), STOCK_FINALIZED, null);
                log.info("[SAGA][FINALIZE][OUT] orderId={} items={}", evt.getOrderId(), items.size());
            } else {
                // 보상 + 전체 예약 해제 + shortage
                for (OrderItem it : succeeded) {
                    try {
                        redis.compensateFinalize(evt.getOrderId(), it.getMenuId(), it.getQty());
                        MenuInventory inv = invRepo.findById(it.getMenuId()).orElse(null);
                        if (inv != null && !inv.isInfiniteStock()) writeThrough.restoreIncrease(it.getMenuId(), it.getQty());
                    } catch (Exception e) {
                        log.error("[SAGA][COMPENSATE][ERR] orderId={} it={} err={}",
                                evt.getOrderId(), it, e.toString());
                    }
                }
                for (OrderItem it : items) {
                    redis.release(evt.getOrderId(), it.getMenuId(), it.getQty());
                }
                var shortage = StockShortageEvent.builder()
                        .eventId(UUID.randomUUID())
                        .orderId(evt.getOrderId())
                        .occurredAt(Instant.now())
                        .items(items)
                        .message("shortage at finalize (paid)")
                        .build();
                producer.sendToStock(shortage, evt.getOrderId().toString(), STOCK_SHORTAGE, null);
                log.info("[SAGA][FINALIZE][SHORTAGE] orderId={}", evt.getOrderId());
            }
        } else if (evt.getStatus() == PaymentStatus.FAILED) {
            // 예약 유지 (재시도 고려)
            log.info("[SAGA][PAYMENT.FAILED] orderId={} (keep reservations)", evt.getOrderId());
        } else {
            log.debug("[SAGA][PAYMENT.IGNORED] orderId={} status={}", evt.getOrderId(), evt.getStatus());
        }
    }

    /** 3) 주문 상태 변경 */
    public void onOrderStatusChanged(OrderStatusChangedEvent evt) {
        if (!redis.markProcessed(evt.getEventId())) {
            log.info("[SAGA][SKIP] order.status.changed dup eventId={}", evt.getEventId());
            return;
        }
        log.info("[SAGA][ORDER.STATUS] orderId={} status={} (payment={})",
                evt.getOrderId(), evt.getStatus(), evt.getPaymentStatus());

        if (evt.getStatus() == OrderStatus.CONFIRMED) {
            log.debug("[SAGA][ORDER.CONFIRMED] orderId={} (no-op)", evt.getOrderId());
            return;
        }

        if (evt.getStatus() == OrderStatus.CANCELED &&
                evt.getPaymentStatus() == PaymentStatus.PAID) {
            // Redis/DB 재회복: 장부 기반으로 복원 수량 산출
            List<OrderItem> ledger = redis.getOrderItems(evt.getOrderId());
            for (OrderItem it : ledger) {
                redis.incrementActual(it.getMenuId(), it.getQty());
                try { writeThrough.restoreIncrease(it.getMenuId(), it.getQty()); }
                catch (Exception e) {
                    log.error("[SAGA][RESTORE][DB-FAIL] orderId={} it={} err={}",
                            evt.getOrderId(), it, e.toString());
                }
            }
            var restored = StockFinalizedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .orderId(evt.getOrderId())
                    .occurredAt(Instant.now())
                    .items(ledger)
                    .build();
            producer.sendToStock(restored, evt.getOrderId().toString(), STOCK_RESTORED, null);
            log.info("[SAGA][RESTORED][OUT] orderId={} items={}", evt.getOrderId(), ledger.size());
        }
    }
}