package com.abdullahkahraman.exchange.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CurrencyCacheService {

    private final RedisTemplate<String, Double> redisTemplate;

    public void setRate(String key, Double rate, long ttlMinutes) {
        redisTemplate.opsForValue().set(key, rate, Duration.ofMinutes(ttlMinutes));
    }

    public Double getRate(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
