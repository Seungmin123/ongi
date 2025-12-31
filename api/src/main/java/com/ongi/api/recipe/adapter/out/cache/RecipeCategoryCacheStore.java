package com.ongi.api.recipe.adapter.out.cache;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RecipeCategoryCacheStore {

	private final StringRedisTemplate redis;

	private static final Duration TTL = Duration.ofDays(7);

	private String key(long recipeId) {
		return "recipe:" + recipeId + ":category";
	}

	/** category가 null/blank면 저장하지 않음 */
	public boolean putIfAbsent(long recipeId, String category) {
		if (category == null) return false;
		String c = category.trim();
		if (c.isEmpty()) return false;

		Boolean ok = redis.opsForValue().setIfAbsent(key(recipeId), c, TTL);
		return Boolean.TRUE.equals(ok);
	}

	public String get(long recipeId) {
		return redis.opsForValue().get(key(recipeId));
	}

	public void evict(long recipeId) {
		redis.delete(key(recipeId));
	}

	/**
	 * 캐시에 없으면 loader로 가져와서 캐시에 넣고 반환
	 * - loader는 DB 호출일 가능성이 크니, Consumer에서만 사용 권장
	 */
	public String getOrLoad(long recipeId, java.util.function.Supplier<String> loader) {
		String cached = get(recipeId);
		if (cached != null) return cached;

		String loaded = loader.get();
		if (loaded == null || loaded.isBlank()) return null;

		putIfAbsent(recipeId, loaded);

		// 다른 스레드가 먼저 넣었을 수 있으니 최종 캐시값을 우선
		String after = get(recipeId);
		return after != null ? after : loaded;
	}
}

