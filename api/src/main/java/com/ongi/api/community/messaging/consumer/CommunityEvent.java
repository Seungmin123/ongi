package com.ongi.api.community.messaging.consumer;

import java.time.LocalDateTime;

public record CommunityEvent(
	String eventId,
	long userId,
	long recipeId,
	String eventType,      // RECIPE_LIKED, RECIPE_UNLIKED, BOOKMARK_CREATED, BOOKMARK_DELETED ...
	LocalDateTime occurredAt
) {}
