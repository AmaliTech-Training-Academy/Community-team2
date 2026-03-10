package com.amalitech.communityboard.config;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Value("${app.jwt.expiration-ms}")
    private  long jwtExpirationMs;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        Cache<Object, Object> tokenCache = Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10_000)
                .expireAfterWrite(jwtExpirationMs, TimeUnit.MILLISECONDS)
                .recordStats()
                .build();
        cacheManager.registerCustomCache("tokenBlacklist", tokenCache);
        return cacheManager;
    }

}
