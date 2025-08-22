package com.example.gateway.resolver;

import com.example.commonservice.security.PrincipalType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

// 사용자 PK 조회 클라이언트(GW -> auth-service)
// 생성자 주입으로 webClient, cache 주입
@Slf4j
@Component
public class UserResolverClient {

    private final WebClient webClient;
    private final Cache<String, ResolveResult> cache;

    @Value("${gateway.user-resolver.base-url}")
    private String baseUrl;

    @Value("${gateway.user-resolver.timeout-ms:1500}")
    private long timeoutMs;

    @Value("${gateway.user-resolver.cache-ttl-sec:60}")
    private long cacheTtlSec;

//    public UserResolverClient() {
//        this.webClient = WebClient.builder().build();
//        this.cache = Caffeine.newBuilder()
//                .expireAfterWrite(Duration.ofSeconds(60)) // 초기 값, 실제 TTL은 아래 set에서 덮어씀
//                .maximumSize(10_000)
//                .build();
//    }
    public UserResolverClient(Cache<String, ResolveResult> cache) {
        this.webClient = WebClient.builder().build();
        this.cache = cache;
    }

    public Mono<ResolveResult> resolveUserId(PrincipalType type, String username) {
        if (!StringUtils.hasText(username)) {
            return Mono.just(ResolveResult.empty());
        }
        final String key = type.name() + ":" + username;

        ResolveResult cached = cache.getIfPresent(key);
        if (cached != null) {
            return Mono.just(cached);
        }

        // 원격 조회
        return webClient.get()
                .uri(baseUrl + "/internal/auth/resolve?principalType={t}&username={u}", type.name(), username)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ResolveResult.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .onErrorResume(ex -> {
                    log.warn("resolveUserId fallback ({}): {}", key, ex.toString());
                    return Mono.just(ResolveResult.empty());
                })
                .doOnNext(res -> {
                    // 성공/실패와 관계없이 캐시해 과도한 호출 방지(원하면 found=true인 경우에만 캐시해도 됨)
                    long ttl = Math.max(1, cacheTtlSec);
                    // Caffeine은 항목별 TTL 설정이 없으므로, 빌더에 지정된 기본 TTL 사용.
                    // 기본 TTL을 갱신하려면 캐시 빌더 생성 시점에 cacheTtlSec을 반영하도록 아래와 같이 재구성.
                    cache.put(key, res);
                });
    }

    // === DTO: record 금지 → class 사용 ===
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ResolveResult {
        private boolean found;
        private UUID userId;
        private String name;

        public ResolveResult(boolean found, UUID userId, String name) {
            this.found = found;
            this.userId = userId;
            this.name = name;
        }

        public static ResolveResult empty() {
            return new ResolveResult(false, null, null);
        }
    }
}