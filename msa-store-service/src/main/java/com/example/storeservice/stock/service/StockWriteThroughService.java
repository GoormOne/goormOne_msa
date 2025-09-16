package com.example.storeservice.stock.service;

import com.example.storeservice.entity.MenuInventory;
import com.example.storeservice.repository.MenuInventoryRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

// Redis finalize 이후 DB availablyQty 증감(write-through) 낙관락 재시도
@Slf4j
@Service
@RequiredArgsConstructor
public class StockWriteThroughService {

    private static final int MAX_RETRY = 5;
    private static final long BASE_BACKOFF_MS = 8L;

    private final MenuInventoryRepository inventoryRepository;
    private final TransactionTemplate tx;

    /** 결제 시점 최종 차감 (Redis finalize 이후) */
    public void finalizeDecrease(UUID menuId, int qty) {
        log.debug("[DB][FINALIZE-DEC] menuId={} qty={}", menuId, qty);
        runWithRetry(() -> {
            tx.executeWithoutResult(status -> {
                MenuInventory inv = inventoryRepository.findById(menuId)
                        .orElseThrow(() -> new IllegalStateException("Inventory not found: " + menuId));
                if (inv.isInfiniteStock()) return; // 무한재고면 DB 차감 생략
                int avail = inv.getAvailableQty();
                if (avail < qty) throw new IllegalStateException("DB shortage: " + menuId);
                inv.setAvailableQty(avail - qty);
                inventoryRepository.save(inv);
                log.debug("[DB][FINALIZE-DEC][OK] menuId={} prev={} now={}", menuId, avail, inv.getAvailableQty());
            });
            return true;
        });
    }

    /** 주문 거절 등으로 복원 */
    public void restoreIncrease(UUID menuId, int qty) {
        log.debug("[DB][RESTORE] menuId={} +{}", menuId, qty);
        runWithRetry(() -> {
            tx.executeWithoutResult(status -> {
                MenuInventory inv = inventoryRepository.findById(menuId)
                        .orElseThrow(() -> new IllegalStateException("Inventory not found: " + menuId));
                if (inv.isInfiniteStock()) return;
                int prev = inv.getAvailableQty();
                inv.setAvailableQty(prev + qty);
                inventoryRepository.save(inv);
                log.debug("[DB][RESTORE][OK] menuId={} prev={} now={}", menuId, prev, inv.getAvailableQty());
            });
            return true;
        });
    }

    private <T> T runWithRetry(Supplier<T> task) {
        int attempt = 0;
        while (true) {
            try {
                return task.get();
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException | StaleObjectStateException e) {
                attempt++;
                if (attempt >= MAX_RETRY) throw e;
                long jitter = ThreadLocalRandom.current().nextLong(0, 8);
                long backoff = (long)(BASE_BACKOFF_MS * Math.pow(2, attempt - 1)) + jitter;
                log.warn("[DB][RETRY] attempt={} backoffMs={}", attempt, backoff);
                try { Thread.sleep(backoff); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
    }
}