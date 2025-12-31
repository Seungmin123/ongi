package com.ongi.api.recipe.web.dto;

import com.ongi.recipe.domain.enums.RecipeCategoryEnum;

public record RecipeDetailBaseResponse(
	String recipeImageUrl,
	String title,
	Integer cookTime,
	String cookTimeText,
	Integer servings,
	String difficulty,
	RecipeCategoryEnum category,
	Long likeCount,
	Long commentCount,
	Long bookmarkCount,

	RecipeIngredientCacheValue ingredients,
	RecipeStepsCacheValue recipeSteps
) {
}
