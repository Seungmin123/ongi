package com.ongi.api.recipe.adapter.out.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeViewCounter {

	private static final String KEY = "recipe:view:delta";
	private final StringRedisTemplate redis;

	public void incr(long recipeId) {
		try {
			redis.opsForHash().increment(KEY, String.valueOf(recipeId), 1L);
		} catch (Exception e) {
			// TODO log.warn("Failed to increment recipe view counter. recipeId={}", recipeId, e);
		}
	}
}
