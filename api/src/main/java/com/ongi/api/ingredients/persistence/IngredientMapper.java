package com.ongi.api.ingredients.persistence;

import com.ongi.ingredients.domain.Ingredient;
import com.ongi.ingredients.domain.IngredientNutrition;
import com.ongi.ingredients.domain.Nutrition;
import com.ongi.ingredients.domain.RecipeIngredient;

public class IngredientMapper {

	public static IngredientEntity toEntity(Ingredient ingredient) {
		return IngredientEntity.builder()
			.name(ingredient.getName())
			.category(ingredient.getCategory())
			.caloriesKcal(ingredient.getCaloriesKcal())
			.carbsG(ingredient.getCarbsG())
			.proteinG(ingredient.getProteinG())
			.fatG(ingredient.getFatG())
			.build();
	}

	public static Ingredient toDomain(IngredientEntity ingredientEntity) {
		return Ingredient.create(ingredientEntity.getName(), ingredientEntity.getCategory(),
			ingredientEntity.getCaloriesKcal(), ingredientEntity.getProteinG(), ingredientEntity.getFatG(), ingredientEntity.getCarbsG());
	}

	public static IngredientNutritionEntity toEntity(IngredientNutrition nutrition) {
		return IngredientNutritionEntity.builder()
			.ingredient(IngredientMapper.toEntity(nutrition.getIngredient()))
			.nutrition(IngredientMapper.toEntity(nutrition.getNutrition()))
			.quantity(nutrition.getQuantity())
			.basis(nutrition.getBasis())
			.build();
	}

	public static IngredientNutrition toDomain(IngredientNutritionEntity entity) {
		return IngredientNutrition.create(IngredientMapper.toDomain(entity.getIngredient()), IngredientMapper.toDomain(entity.getNutrition()), entity.getQuantity(), entity.getBasis());
	}

	public static NutritionEntity toEntity(Nutrition nutrition) {
		return NutritionEntity.builder()
			.code(nutrition.getCode())
			.build();
	}

	public static Nutrition toDomain(NutritionEntity entity){
		return Nutrition.create(entity.getCode());
	}

	public static RecipeIngredientEntity toEntity(RecipeIngredient entity) {
		return RecipeIngredientEntity.builder()
			.recipeId(entity.getRecipeId())
			.ingredientId(entity.getIngredientId())
			.quantity(entity.getQuantity())
			.unit(entity.getUnit())
			.note(entity.getNote())
			.sortOrder(entity.getSortOrder())
			.build();
	}

	public static RecipeIngredient toDomain(RecipeIngredientEntity entity) {
		return RecipeIngredient.create(entity.getRecipeId(), entity.getIngredientId(), entity.getQuantity(), entity.getUnit(), entity.getNote(), entity.getSortOrder());
	}
}
