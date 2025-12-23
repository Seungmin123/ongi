package com.ongi.api.config.cache;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum CacheSpec {

	USER("userCache", Duration.ofMinutes(5)),
	SHORT_LIVED("shortLived", Duration.ofSeconds(30)),
	LONG_LIVED("longLived", Duration.ofHours(1)),
	CONTENT("content", Duration.ofDays(1)),

	RECIPE_LIST("recipeList", Duration.ofMinutes(5)),
	RECIPE_DETAIL("recipeDetail", Duration.ofMinutes(5)),
	RECIPE_INGREDIENT("recipeIngredient", Duration.ofMinutes(5)),
	RECIPE_STEPS("recipeSteps", Duration.ofMinutes(5));

	private final String cacheName;
	private final Duration ttl;

	CacheSpec(String cacheName, Duration ttl) {
		this.cacheName = cacheName;
		this.ttl = ttl;
	}

	public String cacheName() {
		return cacheName;
	}

	public Duration ttl() {
		return ttl;
	}

	/** ConcurrentMapCacheManager용 */
	public static String[] cacheNames() {
		return Arrays.stream(values())
			.map(CacheSpec::cacheName)
			.toArray(String[]::new);
	}

	/** Redis per-cache TTL용 */
	public static Map<String, Duration> ttlMap() {
		return Arrays.stream(values())
			.collect(Collectors.toMap(
				CacheSpec::cacheName,
				CacheSpec::ttl
			));
	}
}
