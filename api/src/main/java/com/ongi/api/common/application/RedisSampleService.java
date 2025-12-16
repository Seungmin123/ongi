//package com.ongi.api.common.application;
//
//import java.time.Duration;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class RedisSampleService {
//
//	private final RedisTemplate<String, String> redisTemplate;
//
//	public void saveSample(String key, String value) {
//		redisTemplate.opsForValue().set(key, value);
//	}
//
//	public String getSample(String key) {
//		return redisTemplate.opsForValue().get(key);
//	}
//
//	public void saveWithTtl(String key, String value, long ttlSeconds) {
//		redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
//	}
//}
