package com.ongi.ingredients.port;

import com.ongi.ingredients.domain.AllergenGroup;
import com.ongi.ingredients.domain.Ingredient;
import com.ongi.ingredients.domain.IngredientNutrition;
import com.ongi.ingredients.domain.Nutrition;
import com.ongi.ingredients.domain.RecipeIngredient;
import com.ongi.ingredients.domain.enums.IngredientCategoryEnum;
import com.ongi.ingredients.domain.enums.NutritionEnum;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IngredientsRepositoryPort {

	Ingredient save(Ingredient ingredient);

	Optional<Ingredient> findIngredientById(Long id);

	Set<Ingredient> findIngredientsByIds(Collection<Long> ingredientIds);

	IngredientNutrition save(IngredientNutrition ingredientNutrition);

	List<IngredientNutrition> saveAllIngredientNutrions(List<IngredientNutrition> ingredientNutritions);

	Optional<IngredientNutrition> findIngredientNutritionById(Long id);

	Nutrition save(Nutrition nutrition);

	List<Nutrition> saveAllNutritions(List<Nutrition> nutritions);

	Optional<Nutrition> findNutritionById(Long id);

	RecipeIngredient save(RecipeIngredient recipeIngredient);

	Optional<RecipeIngredient> findRecipeIngredientById(Long id);

	List<RecipeIngredient> findRecipeIngredientByRecipeId(Long recipeId);

	List<RecipeIngredient> saveAllRecipeIngredients(List<RecipeIngredient> recipeIngredients);

	void deleteRecipeIngredientByRecipeId(Long recipeId);

	Optional<Ingredient> findByName(String name);

	Ingredient findIngredientByName(String name);

	Ingredient findOrCreateIngredient(String name, String code, IngredientCategoryEnum ingredientCategory, Double caloriesKcal, Double proteinG, Double fatG, Double carbsG);

	Ingredient findLikeOrCreateIngredient(String name, IngredientCategoryEnum ingredientCategory, Double caloriesKcal, Double proteinG, Double fatG, Double carbsG);

	Nutrition findNutritionByCode(NutritionEnum code);

	Set<AllergenGroup> findAllergenGroupsByIds(Collection<Long> allergenGroupIds);
}
