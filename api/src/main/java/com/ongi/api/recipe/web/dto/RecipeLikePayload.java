package com.ongi.api.recipe.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecipeLikePayload(
	UUID eventId,
	long recipeId,
	long userId,
	String eventType,
	LocalDateTime occurredAt
) {

}
