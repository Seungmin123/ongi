package com.ongi.api.recipe.web.dto;

import java.util.List;

public record RecipeDetailBaseResponse(
	String recipeImageUrl,
	String title,
	Integer cookTime,
	String cookTimeText,
	Integer servings,
	String difficulty,
	Long likeCount,
	Long commentCount,

	List<RecipeIngredientResponse> ingredients,
	List<RecipeStepsResponse> recipeSteps
) {
}
