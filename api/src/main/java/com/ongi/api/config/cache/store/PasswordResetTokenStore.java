package com.ongi.api.config.cache.store;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenStore {

	private final StringRedisTemplate redis;

	public PasswordResetTokenStore(StringRedisTemplate redis) {
		this.redis = redis;
	}

	private String key(String tokenHash) {
		return "prt:token:" + tokenHash;
	}

	public void put(String tokenHash, String userId, Duration ttl) {
		redis.opsForValue().set(key(tokenHash), userId, ttl);
	}

	public String getUserId(String tokenHash) {
		return redis.opsForValue().get(key(tokenHash));
	}

	public void consume(String tokenHash) {
		redis.delete(key(tokenHash));
	}

	// 토큰 원문을 그대로 저장하지 말고 해시로 저장

}
