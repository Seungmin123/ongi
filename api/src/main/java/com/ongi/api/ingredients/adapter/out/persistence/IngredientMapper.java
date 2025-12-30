package com.ongi.api.ingredients.adapter.out.persistence;

import com.ongi.ingredients.domain.AllergenGroup;
import com.ongi.ingredients.domain.Ingredient;
import com.ongi.ingredients.domain.IngredientAllergen;
import com.ongi.ingredients.domain.IngredientNutrition;
import com.ongi.ingredients.domain.Nutrition;
import com.ongi.ingredients.domain.RecipeIngredient;
import com.ongi.ingredients.domain.enums.RecipeIngredientUnitEnum;

public class IngredientMapper {

	public static IngredientEntity toEntity(Ingredient domain) {
		if (domain.getIngredientId() == null) {
			return IngredientEntity.builder()
				.name(domain.getName())
				.code(domain.getCode())
				.category(domain.getCategory())
				.caloriesKcal(domain.getCaloriesKcal())
				.carbsG(domain.getCarbsG())
				.proteinG(domain.getProteinG())
				.fatG(domain.getFatG())
				.build();
		} else {
			return IngredientEntity.builder()
				.id(domain.getIngredientId())
				.name(domain.getName())
				.code(domain.getCode())
				.category(domain.getCategory())
				.caloriesKcal(domain.getCaloriesKcal())
				.carbsG(domain.getCarbsG())
				.proteinG(domain.getProteinG())
				.fatG(domain.getFatG())
				.build();
		}
	}

	public static Ingredient toDomain(IngredientEntity entity) {
		return Ingredient.create(entity.getId(), entity.getName(), entity.getCode(), entity.getCategory(),
			entity.getCaloriesKcal(), entity.getProteinG(), entity.getFatG(), entity.getCarbsG());
	}

	public static IngredientNutritionEntity toEntity(IngredientNutrition domain, IngredientEntity ingredientRef, NutritionEntity nutritionRef) {
		return IngredientNutritionEntity.builder()
			.id(domain.getId())
			.ingredient(ingredientRef)
			.nutrition(nutritionRef)
			.quantity(domain.getQuantity())
			.basis(domain.getBasis())
			.build();
	}

	public static IngredientNutrition toDomain(IngredientNutritionEntity entity) {
		return IngredientNutrition.create(entity.getId(), IngredientMapper.toDomain(entity.getIngredient()), IngredientMapper.toDomain(entity.getNutrition()), entity.getQuantity(), entity.getBasis());
	}

	public static NutritionEntity toEntity(Nutrition domain) {
		return NutritionEntity.builder()
			.id(domain.getId())
			.code(domain.getCode())
			.unit(domain.getUnit())
			.build();
	}

	public static Nutrition toDomain(NutritionEntity entity){
		return Nutrition.create(entity.getId(), entity.getCode(), entity.getUnit());
	}

	public static RecipeIngredientEntity toEntity(RecipeIngredient entity, IngredientEntity ingredientRef) {
		return RecipeIngredientEntity.builder()
			.id(entity.getId())
			.recipeId(entity.getRecipeId())
			.ingredient(ingredientRef)
			.quantity(entity.getQuantity())
			.unit(entity.getUnit())
			.note(entity.getNote())
			.sortOrder(entity.getSortOrder())
			.build();
	}

	public static RecipeIngredient toDomain(RecipeIngredientEntity entity) {
		return RecipeIngredient.create(entity.getId(), entity.getRecipeId(), IngredientMapper.toDomain(entity.getIngredient()), entity.getQuantity(), entity.getUnit(), entity.getNote(), entity.getSortOrder());
	}

	public static AllergenGroupEntity toEntity(AllergenGroup domain) {
		return AllergenGroupEntity.builder()
			.id(domain.getId())
			.code(domain.getCode())
			.nameKo(domain.getNameKo())
			.build();
	}

	public static AllergenGroup toDomain(AllergenGroupEntity entity){
		return AllergenGroup.create(entity.getId(), entity.getCode(), entity.getNameKo());
	}

	public static IngredientAllergenEntity toEntity(IngredientAllergen domain) {
		return IngredientAllergenEntity.builder()
			.id(domain.getId())
			.ingredientId(domain.getIngredientId())
			.allergenGroupId(domain.getAllergenGroupId())
			.confidence(domain.getConfidence())
			.reason(domain.getReason())
			.build();
	}

	public static IngredientAllergen toDomain(IngredientAllergenEntity entity){
		return IngredientAllergen.create(entity.getId(), entity.getIngredientId(), entity.getAllergenGroupId(), entity.getConfidence(), entity.getReason());
	}

	public static RecipeIngredientUnitEnum mapUnit(String unitStr) {
		if (unitStr == null || unitStr.isBlank()) {
			return RecipeIngredientUnitEnum.TO_TASTE;
		}

		String u = unitStr.trim();

		return switch (u) {

			// === 용량 ===
			case "ml", "ML", "㎖", "밀리리터" -> RecipeIngredientUnitEnum.ML;
			case "L", "l", "리터" -> RecipeIngredientUnitEnum.L;

			// === 무게 ===
			case "g", "G", "그램" -> RecipeIngredientUnitEnum.G;
			case "kg", "KG", "킬로그램" -> RecipeIngredientUnitEnum.KG;

			// === 큰술 (Tablespoon) ===
			case "T", "tbs", "TBS", "Tbsp", "TBSP",
			     "Ts", "TS", "T스푼", "큰술", "스푼" -> RecipeIngredientUnitEnum.TBSP;

			// === 작은술 (Teaspoon) ===
			case "t", "ts", "tsp", "TSP",
			     "작은술", "티스푼" -> RecipeIngredientUnitEnum.TSP;

			// === 컵 ===
			case "컵", "cup", "Cup", "CUP" -> RecipeIngredientUnitEnum.CUP;

			// === 개수 ===
			case "개", "알", "조각", "모", "마리", "덩이" -> RecipeIngredientUnitEnum.PIECE;
			case "팩", "Pack", "PACK" -> RecipeIngredientUnitEnum.PACK;
			case "줌", "다발", "단" -> RecipeIngredientUnitEnum.BUNCH;

			// === 애매한 계량 ===
			case "약간" -> RecipeIngredientUnitEnum.DASH;
			case "꼬집" -> RecipeIngredientUnitEnum.PINCH;
			case "기호에맞게", "기호에", "기호" -> RecipeIngredientUnitEnum.TO_TASTE;

			// === 길이/온도/기타 ===
			case "℃", "°C", "c", "C", "섭씨" -> RecipeIngredientUnitEnum.CELSIUS;
			case "슬라이스" -> RecipeIngredientUnitEnum.SLICE;
			case "장" -> RecipeIngredientUnitEnum.SHEET;

			default -> RecipeIngredientUnitEnum.TO_TASTE;
		};
	}
}
