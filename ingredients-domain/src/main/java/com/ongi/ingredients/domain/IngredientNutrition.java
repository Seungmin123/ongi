package com.ongi.ingredients.domain;

import com.ongi.ingredients.domain.enums.NutritionBasisEnum;

public class IngredientNutrition {

	private Long id;

	private Ingredient ingredient;

	private Nutrition nutrition;

	private Double quantity;

	private NutritionBasisEnum basis;

	public Ingredient getIngredient() {
		return ingredient;
	}

	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	public Nutrition getNutrition() {
		return nutrition;
	}

	public void setNutrition(Nutrition nutrition) {
		this.nutrition = nutrition;
	}

	private IngredientNutrition(
		Ingredient ingredient,
		Nutrition nutrition,
		Double quantity,
		NutritionBasisEnum basis
	) {
		this.ingredient = ingredient;
		this.nutrition = nutrition;
		this.quantity = quantity;
		this.basis = basis;
	}

	public static IngredientNutrition create(
		Ingredient ingredient, Nutrition nutrition, Double quantity, NutritionBasisEnum basis
	) {
		return new IngredientNutrition(ingredient, nutrition, quantity, basis);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIngredientId() {
		return ingredient.getIngredientId();
	}

	public void setIngredientId(Long ingredientId) {
		this.ingredient.setIngredientId(ingredientId);
	}

	public Long getNutritionId() {
		return this.nutrition.getId();
	}

	public void setNutritionId(Long nutritionId) {
		this.nutrition.setId(nutritionId);
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public NutritionBasisEnum getBasis() {
		return basis;
	}

	public void setBasis(NutritionBasisEnum basis) {
		this.basis = basis;
	}
}
