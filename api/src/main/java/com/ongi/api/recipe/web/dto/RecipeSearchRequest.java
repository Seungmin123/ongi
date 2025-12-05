package com.ongi.api.recipe.web.dto;

import com.ongi.recipe.domain.search.RecipeSearch;

public record RecipeSearchRequest(
	String keyword,
	String tag,
	// TODO DB 마이그레이션 및 category Enum화
	String category,
	Long ingredientId,
	Integer maxCookingTimeMin
) {

	public RecipeSearch toSearch() {
		// 우선순위 예시: tag > keyword > category > complex
		if (tag != null && !tag.isBlank()) {
			return new RecipeSearch.ByTag(tag);
		}
		if (keyword != null && !keyword.isBlank()) {
			return new RecipeSearch.ByKeyword(keyword);
		}
		if (category != null && !category.isBlank()) {
			return new RecipeSearch.ByCategory(category);
		}
		if (ingredientId != null) {
			return new RecipeSearch.ByIngredient(ingredientId);
		}
		if (maxCookingTimeMin != null) {
			return new RecipeSearch.ByMaxCookingTimeMin(maxCookingTimeMin);
		}
		// 아무것도 없거나 복합조건으로 처리
		return new RecipeSearch.ByComplex(
			keyword,
			tag,
			category,
			ingredientId,
			maxCookingTimeMin
		);
	}
}
