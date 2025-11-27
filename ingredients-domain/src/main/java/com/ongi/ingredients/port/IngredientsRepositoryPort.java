package com.ongi.ingredients.port;

import com.ongi.ingredients.domain.Ingredient;
import com.ongi.ingredients.domain.IngredientNutrition;
import com.ongi.ingredients.domain.Nutrition;
import com.ongi.ingredients.domain.RecipeIngredient;
import com.ongi.ingredients.domain.enums.IngredientCategoryEnum;
import com.ongi.ingredients.domain.enums.NutritionEnum;
import java.util.List;
import java.util.Optional;

public interface IngredientsRepositoryPort {

	Ingredient save(Ingredient ingredient);

	Optional<Ingredient> findIngredientById(Long id);

	IngredientNutrition save(IngredientNutrition ingredientNutrition);

	Optional<IngredientNutrition> findIngredientNutritionById(Long id);

	Nutrition save(Nutrition nutrition);

	Optional<Nutrition> findNutritionById(Long id);

	RecipeIngredient save(RecipeIngredient recipeIngredient);

	Optional<RecipeIngredient> findRecipeIngredientById(Long id);

	List<RecipeIngredient> saveAll(List<RecipeIngredient> recipeIngredients);

	Optional<Ingredient> findByName(String name);

	Ingredient findIngredientByName(String name);

	Ingredient findOrCreateIngredient(String name, IngredientCategoryEnum ingredientCategory, Double caloriesKcal, Double proteinG, Double fatG, Double carbsG);

	Nutrition findNutritionByCode(NutritionEnum code);
}
