package com.ongi.api.user.adapter.out.cache.store;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisUserEventDedupStore implements UserEventDedupStore {

	private final StringRedisTemplate redis;

	@Override
	public boolean tryConsume(Long userId, String eventId, Duration ttl) {
		String key = "dedup:userEvent:" + userId + ":" + eventId;
		Boolean ok = redis.opsForValue().setIfAbsent(key, "1", ttl);
		return Boolean.TRUE.equals(ok);
	}
}
