package com.ongi.api.user.messaging.consumer;

import java.time.LocalDateTime;

public record UserActionEvent(
	String eventId,
	String eventType,      // RECIPE_LIKED, RECIPE_UNLIKED, BOOKMARK_CREATED, BOOKMARK_DELETED ...
	long userId,
	long recipeId,
	LocalDateTime occurredAt
) {}
