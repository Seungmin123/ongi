package com.ongi.api.recipe.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecipePayload(
	UUID eventId,
	long userId,
	long recipeId,
	String eventType,
	LocalDateTime occurredAt
) {

}
