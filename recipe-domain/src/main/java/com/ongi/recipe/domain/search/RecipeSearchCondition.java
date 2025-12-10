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
}
