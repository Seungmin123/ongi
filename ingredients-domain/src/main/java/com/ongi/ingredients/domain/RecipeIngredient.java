package com.ongi.ingredients.domain;

import com.ongi.ingredients.domain.enums.RecipeIngredientUnitEnum;

public class RecipeIngredient {

	private Long id;

	private Long recipeId;

	private Ingredient ingredient;

	private Double quantity;

	private RecipeIngredientUnitEnum unit;

	private String note;

	private Integer sortOrder;

	private RecipeIngredient(
		Long id,
		Long recipeId,
		Ingredient ingredient,
		Double quantity,
		RecipeIngredientUnitEnum unit,
		String note,
		Integer sortOrder
	) {
		this.id = id;
		this.recipeId = recipeId;
		this.ingredient = ingredient;
		this.quantity = quantity;
		this.unit = unit;
		this.note = note;
		this.sortOrder = sortOrder;
	}

	public static RecipeIngredient create(
		Long id, Long recipeId, Ingredient ingredient, Double quantity, RecipeIngredientUnitEnum unit, String note, Integer sortOrder
	) {
		return new RecipeIngredient(id, recipeId, ingredient, quantity, unit, note, sortOrder);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Ingredient getIngredient() {
		return this.ingredient;
	}

	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	public Long getRecipeId() {
		return this.recipeId;
	}

	public void setRecipeId(Long recipeId) {
		this.recipeId = recipeId;
	}

	public Long getIngredientId() {
		return ingredient.getIngredientId();
	}

	public void setIngredientId(Long ingredientId) {
		this.ingredient.setIngredientId(ingredientId);
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public RecipeIngredientUnitEnum getUnit() {
		return unit;
	}

	public void setUnit(RecipeIngredientUnitEnum unit) {
		this.unit = unit;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}
}
