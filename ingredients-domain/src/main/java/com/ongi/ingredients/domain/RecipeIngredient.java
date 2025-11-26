package com.ongi.ingredients.domain;

import com.ongi.ingredients.domain.enums.RecipeIngredientUnitEnum;
import com.ongi.recipe.domain.Recipe;

public class RecipeIngredient {

	private Long id;

	private Recipe recipe;

	private Ingredient ingredient;

	private Integer quantity;

	private RecipeIngredientUnitEnum unit;

	private String note;

	private Integer sortOrder;

	private RecipeIngredient(
		Recipe recipe,
		Ingredient ingredient,
		Integer quantity,
		RecipeIngredientUnitEnum unit,
		String note,
		Integer sortOrder
	) {
		this.recipe = recipe;
		this.ingredient = ingredient;
		this.quantity = quantity;
		this.unit = unit;
		this.note = note;
		this.sortOrder = sortOrder;
	}

	public static RecipeIngredient create(
		Recipe recipe, Ingredient ingredient, Integer quantity, RecipeIngredientUnitEnum unit, String note, Integer sortOrder
	) {
		return new RecipeIngredient(recipe, ingredient, quantity, unit, note, sortOrder);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Recipe getRecipe() {
		return this.recipe;
	}

	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}

	public Ingredient getIngredient() {
		return this.ingredient;
	}

	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	public Long getRecipeId() {
		return recipe.getId();
	}

	public void setRecipeId(Long recipeId) {
		this.recipe.setId(recipeId);
	}

	public Long getIngredientId() {
		return ingredient.getIngredientId();
	}

	public void setIngredientId(Long ingredientId) {
		this.ingredient.setIngredientId(ingredientId);
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
