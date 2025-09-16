package com.example.storeservice.stock.service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.example.storeservice.stock.model.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Service;

/**
 * Redis 키 설계
 * - actual:   stock:menu:{menuId}:actual       (int) (실제 재고)
 * - reserved: stock:menu:{menuId}:reserved     (int) (메뉴별 합계)
 * - order:    stock:order:{orderId}            (hash: field=menuId, value=qty) (장부)
 * - processed set(idempotency): saga:processed (set: eventId) (멱등성)
 *
 * 정책
 * - reserve: 실제 재고 보지 않고 예약장부/예약합계만 증가
 * - finalize: PAID 시점에만 실제 재고/예약 검증 + 원자 차감
 * - release: 예약 해제
 * - compensateFinalize: 방금 finalize 성공분 복구(보상)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockRedisService {

    private final StringRedisTemplate redis;

    // === Keys ===
    public static String kActual(UUID menuId)   { return "stock:menu:" + menuId + ":actual"; }
    public static String kReserved(UUID menuId) { return "stock:menu:" + menuId + ":reserved"; }
    public static String kOrder(UUID orderId)   { return "stock:order:" + orderId; }
    public static String kProcessed()           { return "saga:processed"; }

    // === Idempotency ===
    public boolean markProcessed(UUID eventId) {
        if (eventId == null) return true; // 헤더만 있는 이벤트와 호환
        Long added = redis.opsForSet().add(kProcessed(), eventId.toString());
        boolean first = added != null && added > 0;
        log.debug("[IDEMPOTENCY] eventId={} first={}", eventId, first);
        return first;
    }

    // === Lua Scripts ===
    // reserve: 실제 재고 확인 없이 예약 장부와 메뉴별 예약합계를 증가
    private static final String LUA_RESERVE = String.join("\n",
            "local reservedKey = KEYS[1]",
            "local orderKey    = KEYS[2]",
            "local menuField   = ARGV[1]",
            "local qty         = tonumber(ARGV[2])",
            "redis.call('HINCRBY', orderKey, menuField, qty)",
            "redis.call('INCRBY', reservedKey, qty)",
            "return 1"
    );

    // release: 장부/예약합계에서 qty만큼 되돌리기 (장부 부족시 0 반환)
    private static final String LUA_RELEASE = String.join("\n",
            "local reservedKey = KEYS[1]",
            "local orderKey    = KEYS[2]",
            "local menuField   = ARGV[1]",
            "local qty         = tonumber(ARGV[2])",
            "local cur = tonumber(redis.call('HGET', orderKey, menuField) or '0')",
            "if cur < qty then return 0 end",
            "redis.call('HINCRBY', orderKey, menuField, -qty)",
            "redis.call('INCRBY', reservedKey, -qty)",
            "return 1"
    );

    // finalize: 결제 시점에만 실재고/예약 검증 후 원자 차감
    private static final String LUA_FINALIZE = String.join("\n",
            "local actualKey   = KEYS[1]",
            "local reservedKey = KEYS[2]",
            "local orderKey    = KEYS[3]",
            "local menuField   = ARGV[1]",
            "local qty         = tonumber(ARGV[2])",
            "local cur = tonumber(redis.call('HGET', orderKey, menuField) or '0')",
            "if cur < qty then return 0 end",
            "local actual   = tonumber(redis.call('GET', actualKey) or '0')",
            "local reserved = tonumber(redis.call('GET', reservedKey) or '0')",
            "if actual < qty then return 0 end",
            "if reserved < qty then return 0 end",
            "redis.call('INCRBY', reservedKey, -qty)",
            "redis.call('INCRBY', actualKey, -qty)",
            "redis.call('HINCRBY', orderKey, menuField, -qty)",
            "return 1"
    );

    // compensateFinalize: 방금 finalize 성공분을 되돌릴 때 사용
    private static final String LUA_COMPENSATE_FINALIZE = String.join("\n",
            "local actualKey   = KEYS[1]",
            "local reservedKey = KEYS[2]",
            "local orderKey    = KEYS[3]",
            "local menuField   = ARGV[1]",
            "local qty         = tonumber(ARGV[2])",
            "redis.call('INCRBY', actualKey, qty)",
            "redis.call('INCRBY', reservedKey, qty)",
            "redis.call('HINCRBY', orderKey, menuField, qty)",
            "return 1"
    );

    private DefaultRedisScript<Long> scriptOf(String code) {
        DefaultRedisScript<Long> s = new DefaultRedisScript<>();
        s.setResultType(Long.class);
        s.setScriptSource(new StaticScriptSource(code));
        return s;
    }

    // === Public API ===
    /** 실재고 확인 없이 예약만 적립 (항상 true 기대) */
    public boolean reserve(UUID orderId, UUID menuId, int qty) {
        log.debug("[REDIS][RESERVE] orderId={} menuId={} qty={}", orderId, menuId, qty); // ☆ BP 후보
        List<String> keys = Arrays.asList(kReserved(menuId), kOrder(orderId));
        Long r = redis.execute(scriptOf(LUA_RESERVE), keys,
                menuId.toString(), Integer.toString(qty));
//        boolean ok = (r != null && r == 1L);
        boolean ok = Long.valueOf(1L).equals(r);
        log.debug("[REDIS][RESERVE] result={} orderKey={} reservedKey={}", ok, kOrder(orderId), kReserved(menuId));
        return ok;
    }

    /** 예약 해제 (부족/보상 등) */
    public boolean release(UUID orderId, UUID menuId, int qty) {
        log.debug("[REDIS][RELEASE] orderId={} menuId={} qty={}", orderId, menuId, qty);
        List<String> keys = Arrays.asList(kReserved(menuId), kOrder(orderId));
        Long r = redis.execute(scriptOf(LUA_RELEASE), keys,
                menuId.toString(), Integer.toString(qty));
        boolean ok = Long.valueOf(1L).equals(r);
        log.debug("[REDIS][RELEASE] result={}", ok);
        return ok;
    }

    /** 결제 완료 시 최종 차감 (검증 포함) */
    public boolean finalizeItem(UUID orderId, UUID menuId, int qty) {
        log.debug("[REDIS][FINALIZE] orderId={} menuId={} qty={}", orderId, menuId, qty); // ☆ BP 후보
        List<String> keys = Arrays.asList(kActual(menuId), kReserved(menuId), kOrder(orderId));
        Long r = redis.execute(scriptOf(LUA_FINALIZE), keys,
                menuId.toString(), Integer.toString(qty));
        boolean ok = Long.valueOf(1L).equals(r);
        log.debug("[REDIS][FINALIZE] result={} actualKey={} reservedKey={} orderKey={}",
                ok, kActual(menuId), kReserved(menuId), kOrder(orderId));
        return ok;
    }

    /** finalize 성공분 되돌리기(복구) */
    public void compensateFinalize(UUID orderId, UUID menuId, int qty) {
        log.debug("[REDIS][COMPENSATE] orderId={} menuId={} qty={}", orderId, menuId, qty);
        List<String> keys = Arrays.asList(kActual(menuId), kReserved(menuId), kOrder(orderId));
        redis.execute(scriptOf(LUA_COMPENSATE_FINALIZE), keys,
                menuId.toString(), Integer.toString(qty));
    }

    /** 주문 장부를 items 리스트로 복원 */
    public List<OrderItem> getOrderItems(UUID orderId) {
        Map<Object,Object> m = redis.opsForHash().entries(kOrder(orderId));
        return m.entrySet().stream()
                .map(e -> OrderItem.builder()
                        .menuId(UUID.fromString(e.getKey().toString()))
                        .qty(Integer.parseInt(e.getValue().toString()))
                        .build())
                .collect(Collectors.toList());
    }

    public void incrementActual(UUID menuId, int qty) {
        log.debug("[REDIS][INCR ACTUAL] menuId={} +{}", menuId, qty);
        redis.opsForValue().increment(kActual(menuId), qty);
    }
}