package com.example.gateway.resolver;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/*
* 생성자 시점 제약 때문에 캐시 TTL이 하드코딩(60초)로 초기화 됨.
* 정확히 cacheTtlSec을 반영하려고 Configuration 빈으로 Cache<String, ResolveResult>를 만들어 주입.
* */
@Configuration
public class UserResolverCacheConfig {

    public static final String USER_RESOLVE_CACHE = "userResolveCache";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager m = new SimpleCacheManager();
        CaffeineCache userCache = new CaffeineCache(
                USER_RESOLVE_CACHE,
                Caffeine.newBuilder()
                        .initialCapacity(100)
                        .maximumSize(10_000)
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .build()
        );
        m.setCaches(List.of(userCache));
        return m;
    }
}