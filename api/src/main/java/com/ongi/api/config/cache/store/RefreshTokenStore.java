package com.ongi.api.config.cache.store;

import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RefreshTokenStore {

	private final StringRedisTemplate redis;

	private String jtiKey(String jti) { return "rt:jti:" + jti; }
	private String userSetKey(String userId) { return "rt:user:" + userId; }

	public void put(String userId, String jti, Duration ttl) {
		redis.opsForValue().set(jtiKey(jti), userId, ttl);
		redis.opsForSet().add(userSetKey(userId), jti);
		// userSetKey 자체 TTL은 선택. (세트 정리는 jti 만료로 자연스럽게 됨)
	}

	public String getUserIdByJti(String jti) {
		return redis.opsForValue().get(jtiKey(jti));
	}

	public void removeJti(String userId, String jti) {
		redis.delete(jtiKey(jti));
		redis.opsForSet().remove(userSetKey(userId), jti);
	}

	public void revokeAll(String userId) {
		Set<String> jtIs = redis.opsForSet().members(userSetKey(userId));
		if (jtIs != null) {
			for (String jti : jtIs) redis.delete(jtiKey(jti));
		}
		redis.delete(userSetKey(userId));
	}
}
