package com.ongi.recipe.domain.search;

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
	// TODO DB 마이그레이션 및 category Enum화
	record ByCategory(String category) implements RecipeSearch {}
	record ByIngredient(Long ingredientId) implements RecipeSearch {}
	record ByMaxCookingTimeMin(Integer maxCookingTimeMin) implements RecipeSearch {}
	record ByComplex(
		String keyword,
		String tag,
		String category,
		Long ingredientId,
		Integer maxCookingTimeMin
	) implements RecipeSearch {}
}
