package com.ongi.api.config.cache.store.user;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SignUpTokenStore {

	private final StringRedisTemplate redis;

	private String tokenKey(String tokenHash) { return "su:token:" + tokenHash; }

	public void put(String tokenHash, String email, Duration ttl) {
		redis.opsForValue().set(tokenKey(tokenHash), email, ttl);
	}

	public String getEmail(String tokenHash) {
		return redis.opsForValue().get(tokenKey(tokenHash));
	}

	public void consume(String tokenHash) {
		redis.delete(tokenKey(tokenHash));
	}
}
