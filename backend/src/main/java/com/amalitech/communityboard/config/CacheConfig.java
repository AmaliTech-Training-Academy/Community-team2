package com.amalitech.communityboard.config;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Value("${app.jwt.expiration-ms}")
    private  long jwtExpirationMs;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        Cache<Object, Object> tokenCache = Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10_000)
                .expireAfterWrite(jwtExpirationMs, TimeUnit.MILLISECONDS)
                .recordStats()
                .build();
        manager.registerCustomCache("tokenBlacklist", tokenCache);

        manager.setCacheNames(List.of(
                "users", "users-page",
                "categories", "categories-page",
                "posts", "posts-filtered",
                "comments", "comments-by-post", "comments-by-user", "comments-all"
        ));

        // Each cache gets its own spec for fine-grained TTL + size control
        manager.registerCustomCache("users",
                buildCache(500, Duration.ofMinutes(15)));

        manager.registerCustomCache("users-page",
                buildCache(100, Duration.ofMinutes(5)));

        manager.registerCustomCache("categories",
                buildCache(200, Duration.ofMinutes(30)));

        manager.registerCustomCache("categories-page",
                buildCache(50, Duration.ofMinutes(10)));

        manager.registerCustomCache("posts",
                buildCache(1000, Duration.ofMinutes(10)));

        manager.registerCustomCache("posts-filtered",
                buildCache(200, Duration.ofMinutes(3)));

        manager.registerCustomCache("comments",
                buildCache(2000, Duration.ofMinutes(10)));

        manager.registerCustomCache("comments-by-post",
                buildCache(300, Duration.ofMinutes(5)));

        manager.registerCustomCache("comments-by-user",
                buildCache(300, Duration.ofMinutes(5)));

        manager.registerCustomCache("comments-all",
                buildCache(50, Duration.ofMinutes(3)));
        return manager;
    }

    private Cache<Object, Object> buildCache(int maxSize, Duration ttl) {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .recordStats()          // enables hit/miss metrics via Actuator
                .build();
    }
}
