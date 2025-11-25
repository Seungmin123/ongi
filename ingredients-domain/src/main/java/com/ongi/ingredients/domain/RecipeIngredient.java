package com.ongi.ingredients.domain;

import com.ongi.ingredients.domain.enums.RecipeIngredientUnitEnum;

public class RecipeIngredient {

	private Long id;

	private Long recipeId;

	private Long ingredientId;

	private Integer quantity;

	private RecipeIngredientUnitEnum unit;

	private String note;

	private Integer sortOrder;

	private RecipeIngredient(
		Long recipeId,
		Long ingredientId,
		Integer quantity,
		RecipeIngredientUnitEnum unit,
		String note,
		Integer sortOrder
	) {
		this.recipeId = recipeId;
		this.ingredientId = ingredientId;
		this.quantity = quantity;
		this.unit = unit;
		this.note = note;
		this.sortOrder = sortOrder;
	}

	public static RecipeIngredient create(
		Long recipeId, Long ingredientId, Integer quantity, RecipeIngredientUnitEnum unit, String note, Integer sortOrder
	) {
		return new RecipeIngredient(recipeId, ingredientId, quantity, unit, note, sortOrder);
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

	public Long getIngredientId() {
		return ingredientId;
	}

	public void setIngredientId(Long ingredientId) {
		this.ingredientId = ingredientId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
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
