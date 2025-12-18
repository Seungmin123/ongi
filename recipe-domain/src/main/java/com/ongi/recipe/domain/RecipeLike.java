package com.ongi.recipe.domain;

public class RecipeLike {

	private Long userId;

	private Long recipeId;

	private RecipeLike(Long userId, Long recipeId) {
		this.userId = userId;
		this.recipeId = recipeId;
	}

	public static RecipeLike create(Long userId, Long recipeId) {
		return new RecipeLike(userId, recipeId);
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
