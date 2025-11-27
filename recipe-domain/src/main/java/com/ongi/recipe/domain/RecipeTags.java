package com.ongi.recipe.domain;

public class RecipeTags {

	private Long id;

	private Long recipeId;

	private String tag;

	private RecipeTags(
		Long id,
		Long recipeId,
		String tag
	) {
		this.id = id;
		this.recipeId = recipeId;
		this.tag = tag;
	}

	public static RecipeTags create(
		Long id, Long recipeId, String tag
	) {
		return new RecipeTags(id, recipeId, tag);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(Long recipeId) {
		this.recipeId = recipeId;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}
