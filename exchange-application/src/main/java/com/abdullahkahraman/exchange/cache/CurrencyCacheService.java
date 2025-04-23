package com.abdullahkahraman.exchange.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CurrencyCacheService {

    private final RedisTemplate<String, Double> redisTemplate;

    /**
     * Sets a currency rate in the cache with a specified time-to-live (TTL) duration.
     *
     * @param key the key under which the rate will be stored
     * @param rate the currency rate to store
     * @param ttlMinutes the time-to-live duration for the cache entry, in minutes
     */
    public void setRate(String key, Double rate, long ttlMinutes) {
        redisTemplate.opsForValue().set(key, rate, Duration.ofMinutes(ttlMinutes));
    }

    /**
     * Retrieves a currency rate from the cache for the specified key.
     *
     * @param key the key associated with the currency rate to retrieve
     * @return the currency rate associated with the given key, or null if no value exists for the key
     */
    public Double getRate(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Checks if a key exists in the Redis cache.
     *
     * @param key the key to check for existence in the cache
     * @return true if the key exists in the cache, false otherwise
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
