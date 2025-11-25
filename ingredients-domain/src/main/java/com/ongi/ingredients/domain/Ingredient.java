package com.ongi.ingredients.domain;

import com.ongi.ingredients.domain.enums.IngredientCategoryEnum;

public class Ingredient {

	private Long ingredientId;

	private String name;

	private IngredientCategoryEnum category;

	private Ingredient(
		String name,
		IngredientCategoryEnum category
	) {
		this.name = name;
		this.category = category;
	}

	public static Ingredient create(
		String name, IngredientCategoryEnum category
	) {
		return new Ingredient(name, category);
	}

	public Long getIngredientId() {
		return ingredientId;
	}

	public void setIngredientId(Long ingredientId) {
		this.ingredientId = ingredientId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IngredientCategoryEnum getCategory() {
		return category;
	}

	public void setCategory(IngredientCategoryEnum category) {
		this.category = category;
	}
}
