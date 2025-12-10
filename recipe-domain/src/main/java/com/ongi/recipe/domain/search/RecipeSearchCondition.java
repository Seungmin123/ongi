package com.ongi.recipe.domain.search;

import com.ongi.recipe.domain.enums.RecipeCategoryEnum;

public class RecipeSearchCondition {

	private final String keyword;
	private final String tag;
	private final RecipeCategoryEnum category;
	private final Long ingredientId;
	private final Integer maxCookingTimeMin;

	public RecipeSearchCondition(
		String keyword,
		String tag,
		RecipeCategoryEnum category,
		Long ingredientId,
		Integer maxCookingTimeMin
	) {
		this.keyword = keyword;
		this.tag = tag;
		this.category = category;
		this.ingredientId = ingredientId;
		this.maxCookingTimeMin = maxCookingTimeMin;
	}

	public String getKeyword() { return keyword; }
	public String getTag() { return tag; }
	public RecipeCategoryEnum getCategory() { return category; }
	public Long getIngredientId() { return ingredientId; }
	public Integer getMaxCookingTimeMin() { return maxCookingTimeMin; }

	public static RecipeSearchCondition empty() {
		return new RecipeSearchCondition(null, null, null, null, null);
	}

	public String toKeyString() {
		return String.join(":",
			keyword != null ? keyword : "",
			tag != null ? tag : "",
			category != null ? category.name() : "",
			ingredientId != null ? ingredientId.toString() : "",
			maxCookingTimeMin != null ? maxCookingTimeMin.toString() : ""
		);
	}

	public static RecipeSearchCondition from(RecipeSearch search) {
		if (search == null) {
			return empty();
		}

		return switch (search) {
			case RecipeSearch.ByKeyword s -> new RecipeSearchCondition(
				s.keyword(), null, null, null, null
			);

			case RecipeSearch.ByTag s -> new RecipeSearchCondition(
				null, s.tag(), null, null, null
			);

			case RecipeSearch.ByCategory s -> new RecipeSearchCondition(
				null, null, s.category(), null, null
			);

			case RecipeSearch.ByIngredient s -> new RecipeSearchCondition(
				null, null, null, s.ingredientId(), null
			);

			case RecipeSearch.ByMaxCookingTimeMin s -> new RecipeSearchCondition(
				null, null, null, null, s.maxCookingTimeMin()
			);

			case RecipeSearch.ByComplex s -> new RecipeSearchCondition(
				s.keyword(), s.tag(), s.category(), s.ingredientId(), s.maxCookingTimeMin()
			);
		};
	}
}
