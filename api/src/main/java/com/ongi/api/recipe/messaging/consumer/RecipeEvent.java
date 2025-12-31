package com.ongi.api.recipe.messaging.consumer;

import java.time.LocalDateTime;

public record RecipeEvent(
	String eventId,
	long userId,
	long recipeId,
	String eventType,      // RECIPE_LIKED, RECIPE_UNLIKED, BOOKMARK_CREATED, BOOKMARK_DELETED ...
	String category,
	LocalDateTime occurredAt
) {}
