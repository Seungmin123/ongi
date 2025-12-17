package com.ongi.api.recipe.messaging.consumer;

import java.time.LocalDateTime;

public record RecipeLikeEvent(
	String eventId,
	String eventType,      // RECIPE_LIKED, RECIPE_UNLIKED, BOOKMARK_CREATED, BOOKMARK_DELETED ...
	long userId,
	long recipeId,
	LocalDateTime occurredAt
) {}
