package com.ongi.api.config.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("local")
@Component("cacheVersion")
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false", matchIfMissing = true)
public class LocalCacheVersion implements CacheVersion {
	private final ConcurrentHashMap<Object, AtomicLong> map = new ConcurrentHashMap<>();

	@Override
	public String version(Long pageUid) {
		return String.valueOf(map.computeIfAbsent(pageUid, k -> new AtomicLong(1)).get());
	}

	@Override
	public void bumpVersion(Long pageUid) {
		map.computeIfAbsent(pageUid, k -> new AtomicLong(1)).incrementAndGet();
	}

	@Override
	public String contentVersion(String contentId) {
		return String.valueOf(map.computeIfAbsent(contentId, k -> new AtomicLong(1)).get());
	}

	@Override
	public void bumpContentVersion(String contentId) {
		map.computeIfAbsent(contentId, k -> new AtomicLong(1)).incrementAndGet();
	}
}
