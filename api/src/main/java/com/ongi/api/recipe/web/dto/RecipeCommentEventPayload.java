package com.ongi.api.recipe.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecipeCommentEventPayload(
	UUID eventId,
	long recipeId,
	long commentId,
	long userId,
	String eventType,
	LocalDateTime occurredAt
) {

}
