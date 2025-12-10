package com.ongi.api.recipe.web.dto;

public record RecipeIngredientResponse(
	Long ingredientId,
	String name,
	Double quantity,
	String unit,
	String note
) {
}
