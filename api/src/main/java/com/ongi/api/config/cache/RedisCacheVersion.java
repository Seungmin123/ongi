package com.ongi.api.config.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component("cacheVersion")
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
@RequiredArgsConstructor
public class RedisCacheVersion implements CacheVersion {

	private final StringRedisTemplate redis;

	private static final String KEY_PREFIX = "content:version:pageUid:";

	private static final String CONTENT_KEY_PREFIX = "content:version:contentId:";

	@Override
	public String version(Long pageUid) {
		String key = KEY_PREFIX + pageUid;
		String v = redis.opsForValue().get(key);
		if (v == null) {
			// 초기값 1 생성 (경쟁조건 방지용 setIfAbsent)
			Boolean ok = redis.opsForValue().setIfAbsent(key, "1");
			v = (ok != null && ok) ? "1" : redis.opsForValue().get(key);
			if (v == null) v = "1";
		}
		return v;
	}

	@Override
	public void bumpVersion(Long pageUid) {
		redis.opsForValue().increment(KEY_PREFIX + pageUid);
	}

	@Override
	public String contentVersion(String contentId) {
		String key = CONTENT_KEY_PREFIX + contentId;
		String v = redis.opsForValue().get(key);
		if (v == null) {
			// 초기값 1 생성 (경쟁조건 방지용 setIfAbsent)
			Boolean ok = redis.opsForValue().setIfAbsent(key, "1");
			v = (ok != null && ok) ? "1" : redis.opsForValue().get(key);
			if (v == null) v = "1";
		}
		return v;
	}

	@Override
	public void bumpContentVersion(String contentId) {
		redis.opsForValue().increment(CONTENT_KEY_PREFIX + contentId);
	}
}
