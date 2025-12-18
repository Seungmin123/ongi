package com.ongi.api.recipe.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecipeCommentEventPayload(
	UUID eventId,
	long userId,
	long recipeId,
	long commentId,
	String eventType,
	LocalDateTime occurredAt
) {

}
