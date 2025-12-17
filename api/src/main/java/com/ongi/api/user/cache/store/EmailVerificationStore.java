package com.ongi.api.user.cache.store;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EmailVerificationStore {

	private final StringRedisTemplate redis;

	private String codeKey(String email) { return "ev:code:" + email; }
	private String attemptKey(String email) { return "ev:attempt:" + email; }

	public void putCodeHash(String email, String codeHash, Duration ttl) {
		redis.opsForValue().set(codeKey(email), codeHash, ttl);
		// 시도횟수 키도 TTL을 동일하게 맞춰 관리
		redis.opsForValue().setIfAbsent(attemptKey(email), "0", ttl);
	}

	public String getCodeHash(String email) {
		return redis.opsForValue().get(codeKey(email));
	}

	public long incrAttempt(String email) {
		return redis.opsForValue().increment(attemptKey(email));
	}

	public void clear(String email) {
		redis.delete(codeKey(email));
		redis.delete(attemptKey(email));
	}
}
