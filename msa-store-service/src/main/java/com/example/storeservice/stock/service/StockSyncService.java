package com.example.storeservice.stock.service;

import com.example.storeservice.entity.MenuInventory;
import com.example.storeservice.repository.MenuInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockSyncService {

    private final MenuInventoryRepository invRepo;
    private final StringRedisTemplate redis;

    public void syncFromDb() {
        log.info("[SYNC][START] DB -> Redis");
        List<MenuInventory> all = invRepo.findAll();
        for (MenuInventory inv : all) {
            String actualKey = StockRedisService.kActual(inv.getMenuId());
            String reservedKey = StockRedisService.kReserved(inv.getMenuId());
            if (inv.isInfiniteStock()) {
                redis.opsForValue().set(actualKey, String.valueOf(Integer.MAX_VALUE));
            } else {
                redis.opsForValue().set(actualKey, String.valueOf(inv.getAvailableQty()));
            }
            redis.opsForValue().setIfAbsent(reservedKey, "0");
            log.debug("[SYNC][SET] menuId={} actual={} reserved=0",
                    inv.getMenuId(), inv.isInfiniteStock() ? "INF" : inv.getAvailableQty());
        }
        log.info("[SYNC][END] synced {} items", all.size());
    }
}
