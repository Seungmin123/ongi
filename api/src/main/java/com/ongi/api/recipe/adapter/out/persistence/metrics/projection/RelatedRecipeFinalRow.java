package com.ongi.api.recipe.adapter.out.persistence.metrics.projection;

public record RelatedRecipeFinalRow(
	Long recipeId,
	double ingredientIdfScore,
	long view7d,
	double finalScore
) {

}
