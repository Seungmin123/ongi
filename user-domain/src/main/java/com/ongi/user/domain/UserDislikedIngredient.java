package com.ongi.user.domain;

public class UserDislikedIngredient {

	private Long id;

	private Long userId;

	private Long ingredientId;

	private UserDislikedIngredient(Long id, Long userId, Long ingredientId) {
		this.id = id;
		this.userId = userId;
		this.ingredientId = ingredientId;
	}

	public static UserDislikedIngredient create(Long id, Long userId, Long ingredientId) {
		return new UserDislikedIngredient(id, userId, ingredientId);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getIngredientId() {
		return ingredientId;
	}

	public void setIngredientId(Long ingredientId) {
		this.ingredientId = ingredientId;
	}
}
