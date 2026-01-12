package com.example.storeservice.stock;


import com.example.storeservice.stock.service.StockSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"local","dev","default"}) // 필요 프로필에 맞게 수정
public class StockStartupSync implements ApplicationRunner {

    private final StockSyncService stockSyncService;

    @Value("${stock.sync-on-start:true}")
    private boolean syncOnStart;

    @Override
    public void run(ApplicationArguments args) {
        if (!syncOnStart) {
            log.info("[SYNC] skip on start (stock.sync-on-start=false)");
            return;
        }
        log.info("[SYNC] starting DB -> Redis initial sync...");
        stockSyncService.syncFromDb();
        log.info("[SYNC] completed.");
    }
}
