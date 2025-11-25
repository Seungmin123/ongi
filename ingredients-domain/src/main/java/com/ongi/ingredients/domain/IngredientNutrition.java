package com.ongi.ingredients.domain;

public class IngredientNutrition {

	private Long id;

	private Long ingredientId;

	private Long nutritionId;

	private IngredientNutrition(
		Long ingredientId,
		Long nutritionId
	) {
		this.ingredientId = ingredientId;
		this.nutritionId = nutritionId;
	}

	public static IngredientNutrition create(
		Long ingredientId, Long nutritionId
	) {
		return new IngredientNutrition(ingredientId, nutritionId);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIngredientId() {
		return ingredientId;
	}

	public void setIngredientId(Long ingredientId) {
		this.ingredientId = ingredientId;
	}

	public Long getNutritionId() {
		return nutritionId;
	}

	public void setNutritionId(Long nutritionId) {
		this.nutritionId = nutritionId;
	}
}
