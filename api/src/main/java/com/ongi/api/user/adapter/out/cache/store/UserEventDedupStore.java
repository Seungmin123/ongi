package com.ongi.api.user.adapter.out.cache.store;

import java.time.Duration;

public interface UserEventDedupStore {
	boolean tryConsume(Long userId, String eventId, Duration ttl);
}
