package com.ongi.api.config.cache;

public interface CacheVersion {

	String version(Long pageUid);

	void bumpVersion(Long pageUid);

	String contentVersion(String contentId);

	void bumpContentVersion(String contentId);
}
