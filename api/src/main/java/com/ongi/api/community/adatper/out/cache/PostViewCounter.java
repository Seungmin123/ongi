package com.ongi.api.community.adatper.out.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PostViewCounter {

	private static final String KEY = "community:post:view:delta";

	private final StringRedisTemplate redis;

	public void incr(long postId) {
		try {
			redis.opsForHash().increment(KEY, String.valueOf(postId), 1L);
		} catch (Exception e) {
			// TODO log.warn("Failed to increment recipe view counter. postId={}", postId, e);
		}
	}
}
