package com.ongi.api.ingredients.persistence;

import com.ongi.api.recipe.persistence.RecipeMapper;
import com.ongi.ingredients.domain.Ingredient;
import com.ongi.ingredients.domain.IngredientNutrition;
import com.ongi.ingredients.domain.Nutrition;
import com.ongi.ingredients.domain.RecipeIngredient;
import com.ongi.ingredients.domain.enums.RecipeIngredientUnitEnum;

public class IngredientMapper {

	public static IngredientEntity toEntity(Ingredient ingredient) {
		return IngredientEntity.builder()
			.id(ingredient.getIngredientId())
			.name(ingredient.getName())
			.category(ingredient.getCategory())
			.caloriesKcal(ingredient.getCaloriesKcal())
			.carbsG(ingredient.getCarbsG())
			.proteinG(ingredient.getProteinG())
			.fatG(ingredient.getFatG())
			.build();
	}

	public static Ingredient toDomain(IngredientEntity ingredientEntity) {
		return Ingredient.create(ingredientEntity.getId(), ingredientEntity.getName(), ingredientEntity.getCategory(),
			ingredientEntity.getCaloriesKcal(), ingredientEntity.getProteinG(), ingredientEntity.getFatG(), ingredientEntity.getCarbsG());
	}

	public static IngredientNutritionEntity toEntity(IngredientNutrition domain) {
		return IngredientNutritionEntity.builder()
			.id(domain.getId())
			.ingredient(IngredientMapper.toEntity(domain.getIngredient()))
			.nutrition(IngredientMapper.toEntity(domain.getNutrition()))
			.quantity(domain.getQuantity())
			.basis(domain.getBasis())
			.build();
	}

	public static IngredientNutrition toDomain(IngredientNutritionEntity entity) {
		return IngredientNutrition.create(entity.getId(), IngredientMapper.toDomain(entity.getIngredient()), IngredientMapper.toDomain(entity.getNutrition()), entity.getQuantity(), entity.getBasis());
	}

	public static NutritionEntity toEntity(Nutrition nutrition) {
		return NutritionEntity.builder()
			.id(nutrition.getId())
			.code(nutrition.getCode())
			.unit(nutrition.getUnit())
			.build();
	}

	public static Nutrition toDomain(NutritionEntity entity){
		return Nutrition.create(entity.getId(), entity.getCode(), entity.getUnit());
	}

	public static RecipeIngredientEntity toEntity(RecipeIngredient entity) {
		return RecipeIngredientEntity.builder()
			.id(entity.getId())
			.recipeId(entity.getRecipeId())
			.ingredient(IngredientMapper.toEntity(entity.getIngredient()))
			.quantity(entity.getQuantity())
			.unit(entity.getUnit())
			.note(entity.getNote())
			.sortOrder(entity.getSortOrder())
			.build();
	}

	public static RecipeIngredient toDomain(RecipeIngredientEntity entity) {
		return RecipeIngredient.create(entity.getId(), entity.getRecipeId(), IngredientMapper.toDomain(entity.getIngredient()), entity.getQuantity(), entity.getUnit(), entity.getNote(), entity.getSortOrder());
	}

	public static RecipeIngredientUnitEnum mapUnit(String unitStr) {
		if (unitStr == null || unitStr.isBlank()) {
			// 애매하면 일단 "기호에 맞게"로 처리
			return RecipeIngredientUnitEnum.TO_TASTE;
		}

		String u = unitStr.trim().toLowerCase();

		return switch (u) {
			// 용량
			case "ml", "밀리리터" -> RecipeIngredientUnitEnum.ML;
			case "l", "리터" -> RecipeIngredientUnitEnum.L;

			// 무게
			case "g", "그램" -> RecipeIngredientUnitEnum.G;
			case "kg", "킬로그램" -> RecipeIngredientUnitEnum.KG;

			// 계량
			case "큰술", "스푼", "tbsp" -> RecipeIngredientUnitEnum.TBSP;
			case "작은술", "티스푼", "tsp" -> RecipeIngredientUnitEnum.TSP;
			case "컵", "cup" -> RecipeIngredientUnitEnum.CUP;

			// 개수
			case "개", "알", "조각", "모", "마리", "덩이" -> RecipeIngredientUnitEnum.PIECE;
			case "팩", "pack" -> RecipeIngredientUnitEnum.PACK;
			case "줌", "다발", "단" -> RecipeIngredientUnitEnum.BUNCH;

			// 애매
			case "약간" -> RecipeIngredientUnitEnum.DASH;
			case "꼬집" -> RecipeIngredientUnitEnum.PINCH;
			case "기호에맞게", "기호에", "기호" -> RecipeIngredientUnitEnum.TO_TASTE;

			// 온도
			case "c", "°c", "섭씨" -> RecipeIngredientUnitEnum.CELSIUS;

			// 길이/사이즈
			case "슬라이스" -> RecipeIngredientUnitEnum.SLICE;
			case "장" -> RecipeIngredientUnitEnum.SHEET;

			default -> RecipeIngredientUnitEnum.TO_TASTE; // 모르면 일단 애매한 계량으로
		};
	}
}
