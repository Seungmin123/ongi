package com.ongi.ingredients.domain;

import com.ongi.ingredients.domain.enums.IngredientCategoryEnum;

public class Ingredient {

	private Long ingredientId;

	private String name;

	private IngredientCategoryEnum category;

	private Double caloriesKcal;

	private Double proteinG;

	private Double fatG;

	private Double carbsG;

	private Ingredient(
		String name,
		IngredientCategoryEnum category,
		Double caloriesKcal,
		Double proteinG,
		Double fatG,
		Double carbsG
	) {
		this.name = name;
		this.category = category;
		this.caloriesKcal = caloriesKcal;
		this.proteinG = proteinG;
		this.fatG = fatG;
		this.carbsG = carbsG;
	}

	public static Ingredient create(
		String name, IngredientCategoryEnum category, Double caloriesKcal, Double proteinG, Double fatG, Double carbsG
	) {
		return new Ingredient(name, category, caloriesKcal, proteinG, fatG, carbsG);
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

	public Double getCaloriesKcal() {
		return caloriesKcal;
	}

	public void setCaloriesKcal(Double caloriesKcal) {
		this.caloriesKcal = caloriesKcal;
	}

	public Double getProteinG() {
		return proteinG;
	}

	public void setProteinG(Double proteinG) {
		this.proteinG = proteinG;
	}

	public Double getFatG() {
		return fatG;
	}

	public void setFatG(Double fatG) {
		this.fatG = fatG;
	}

	public Double getCarbsG() {
		return carbsG;
	}

	public void setCarbsG(Double carbsG) {
		this.carbsG = carbsG;
	}
}
