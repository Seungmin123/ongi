package com.ongi.recipe.domain.search;

public class RecipeSearchCondition {

	private final String keyword;
	private final String tag;
	// TODO DB 마이그레이션 및 category Enum화
	private final String category;
	private final Long ingredientId;
	private final Integer maxCookingTimeMin;

	public RecipeSearchCondition(
		String keyword,
		String tag,
		String category,
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
	public String getCategory() { return category; }
	public Long getIngredientId() { return ingredientId; }
	public Integer getMaxCookingTimeMin() { return maxCookingTimeMin; }

	public static RecipeSearchCondition empty() {
		return new RecipeSearchCondition(null, null, null, null, null);
	}
}
