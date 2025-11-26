package com.ongi.api.config.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!local")
@Component("cacheVersion")
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false", matchIfMissing = true)
public class NoopCacheVersion implements CacheVersion {

	@Override
	public String version(Long pageUid) {
		return "1";
	}

	@Override
	public void bumpVersion(Long pageUid) {
	}

	@Override
	public String contentVersion(String contentId) {
		return "1";
	}

	@Override
	public void bumpContentVersion(String contentId) {
	}
}
