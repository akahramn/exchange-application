package com.abdullahkahraman.exchange.cache;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CurrencyCacheService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyCacheService.class);

    private final RedisTemplate<String, Double> redisTemplate;

    /**
     * Sets a currency rate in the cache with a specified time-to-live (TTL) duration.
     *
     * @param key the key under which the rate will be stored
     * @param rate the currency rate to store
     * @param ttlMinutes the time-to-live duration for the cache entry, in minutes
     */
    public void setRate(String key, Double rate, long ttlMinutes) {
        try {
            redisTemplate.opsForValue().set(key, rate, Duration.ofMinutes(ttlMinutes));
            logger.info("Exchange rate cached successfully for key='{}'", key);
        } catch (Exception e) {
            logger.error("Failed to cache exchange rate for key='{}': {}", key, e.getMessage(), e);
        }
    }

    /**
     * Retrieves a currency rate from the cache for the specified key.
     *
     * @param key the key associated with the currency rate to retrieve
     * @return the currency rate associated with the given key, or null if no value exists for the key
     */
    public Double getRate(String key) {
        try {
            Double rate = redisTemplate.opsForValue().get(key);

            if (rate != null) {
                logger.info("Cache exist for key='{}', rate={}", key, rate);
            } else {
                logger.warn("Cache does not exist for key='{}'", key);
            }

            return rate;

        } catch (Exception e) {
            logger.error("Failed to retrieve rate from cache for key='{}': {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Checks if a key exists in the Redis cache.
     *
     * @param key the key to check for existence in the cache
     * @return true if the key exists in the cache, false otherwise
     */
    public boolean exists(String key) {
        try {
            boolean exists = redisTemplate.hasKey(key);

            if (exists) {
                logger.info("Cache key exists: '{}'", key);
            } else {
                logger.warn("Cache key does not exist: '{}'", key);
            }

            return exists;

        } catch (Exception e) {
            logger.error("Failed to check existence of key='{}': {}", key, e.getMessage(), e);
            return false;
        }
    }
}
