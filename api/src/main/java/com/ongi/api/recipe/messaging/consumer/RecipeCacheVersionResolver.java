package com.ongi.api.recipe.messaging.consumer;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeCacheVersionResolver {

	private final StringRedisTemplate redisTemplate;

	// TODO YML 주입
	private static final String VERSION_KEY = "prod:recipe-api:v1:recipe:%d:cachever";

	private static final Duration VERSION_TTL = Duration.ofDays(30);

	public int getOrInit(long recipeId) {
		String key = String.format(VERSION_KEY, recipeId);

		String v = redisTemplate.opsForValue().get(key);
		if (v != null) return parseIntOr1(v);

		// setIfAbsent로 동시성 안전하게 초기화
		Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, "1", VERSION_TTL);
		if (Boolean.TRUE.equals(ok)) return 1;

		// 다른 스레드가 초기화했을 수 있으니 재조회
		v = redisTemplate.opsForValue().get(key);
		return v == null ? 1 : parseIntOr1(v);
	}

	/**
	 * 레시피 관련 변경 발생 시 호출.
	 * INCR로 버전을 올려 기존 캐시 키를 전부 무력화.
	 */
	public int bump(long recipeId) {
		String key = String.format(VERSION_KEY, recipeId);

		Long newVal = redisTemplate.opsForValue().increment(key);
		if (newVal == null) {
			// Redis 장애/이상 케이스: 안전하게 1로
			redisTemplate.opsForValue().set(key, "1", VERSION_TTL);
			return 1;
		}

		// increment는 TTL을 건드리지 않는 경우가 있어, 안전하게 TTL 보정
		// (이미 TTL이 있으면 불필요하지만, 만료가 없다면 붙여줌)
		Long ttl = redisTemplate.getExpire(key);
		if (ttl == null || ttl < 0) {
			redisTemplate.expire(key, VERSION_TTL);
		}

		// int 캐스팅 안전 범위에서만 사용(실제로 수십억까지 갈 일 없음)
		return newVal.intValue();
	}

	/**
	 * 선택: 특정 레시피를 강제로 초기화하고 싶을 때.
	 */
	public void reset(long recipeId) {
		String key = String.format(VERSION_KEY, recipeId);
		redisTemplate.opsForValue().set(key, "1", VERSION_TTL);
	}

	private int parseIntOr1(String v) {
		try {
			return Integer.parseInt(v);
		} catch (NumberFormatException e) {
			return 1;
		}
	}
}
