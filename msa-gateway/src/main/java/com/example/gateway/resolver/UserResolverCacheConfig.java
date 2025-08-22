package com.example.gateway.resolver;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/*
* 생성자 시점 제약 때문에 캐시 TTL이 하드코딩(60초)로 초기화 됨.
* 정확히 cacheTtlSec을 반영하려고 Configuration 빈으로 Cache<String, ResolveResult>를 만들어 주입.
* */
@Configuration
public class UserResolverCacheConfig {

    @Bean
    public Cache<String, UserResolverClient.ResolveResult> userResolverCache(
            @Value("${gateway.user-resolver.cache-ttl-sec:60}") long cacheTtlSec
    ) {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(Math.max(1, cacheTtlSec)))
                .maximumSize(10_000)
                .build();
    }
}