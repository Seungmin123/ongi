package com.ongi.api.recipe.web.dto;

public record RecipeStepsResponse(
	Long stepId,
	Integer stepOrder,
	String title,
	String description,
	Integer estimatedMin,
	Integer waitMin,
	String temperature,
	String imageUrl,
	String videoUrl
) {
}
