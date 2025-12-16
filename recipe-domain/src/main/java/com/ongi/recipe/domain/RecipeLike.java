package com.ongi.recipe.domain;

public class RecipeLike {

	private Long recipeId;

	private Long userId;

	private RecipeLike(Long recipeId, Long userId) {
		this.recipeId = recipeId;
		this.userId = userId;
	}

	public static RecipeLike create(Long recipeId, Long userId) {
		return new RecipeLike(recipeId, userId);
	}

	public Long getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(Long recipeId) {
		this.recipeId = recipeId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
