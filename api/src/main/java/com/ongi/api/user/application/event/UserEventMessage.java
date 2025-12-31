package com.ongi.api.user.application.event;

import com.ongi.user.domain.enums.UserEventType;
import java.time.Instant;
import java.util.Map;

public record UserEventMessage(
	Long userId,
	String sessionId,
	String deviceId,
	String clientTs,

	String eventId,
	UserEventType type,
	String occurredAt,

	String pageKey,
	Long recipeId,
	String referrer,

	Instant serverReceivedAt,
	Map<String, Object> props
) {}
