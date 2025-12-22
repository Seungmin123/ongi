package com.ongi.api.recipe.web.dto;

public record RecipeDetailBaseResponse(
	String recipeImageUrl,
	String title,
	Integer cookTime,
	String cookTimeText,
	Integer servings,
	String difficulty,
	Long likeCount,
	Long commentCount,
	Long bookmarkCount,

	RecipeIngredientCacheValue ingredients,
	RecipeStepsCacheValue recipeSteps
) {
}
