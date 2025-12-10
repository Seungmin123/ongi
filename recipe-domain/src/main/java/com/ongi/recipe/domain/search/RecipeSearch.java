package com.ongi.recipe.domain.search;

import com.ongi.recipe.domain.enums.RecipeCategoryEnum;

public sealed interface RecipeSearch permits
	RecipeSearch.ByKeyword,
	RecipeSearch.ByTag,
	RecipeSearch.ByCategory,
	RecipeSearch.ByIngredient,
	RecipeSearch.ByMaxCookingTimeMin,
	RecipeSearch.ByComplex
{
	record ByKeyword(String keyword) implements RecipeSearch {}
	record ByTag(String tag) implements RecipeSearch {}
	record ByCategory(RecipeCategoryEnum category) implements RecipeSearch {}
	record ByIngredient(Long ingredientId) implements RecipeSearch {}
	record ByMaxCookingTimeMin(Integer maxCookingTimeMin) implements RecipeSearch {}
	record ByComplex(
		String keyword,
		String tag,
		RecipeCategoryEnum category,
		Long ingredientId,
		Integer maxCookingTimeMin
	) implements RecipeSearch {}
}
