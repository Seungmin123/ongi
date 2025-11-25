package com.ongi.api.ingredients.persistence;

import com.ongi.api.ingredients.persistence.repository.IngredientNutritionRepository;
import com.ongi.api.ingredients.persistence.repository.IngredientRepository;
import com.ongi.api.ingredients.persistence.repository.NutritionRepository;
import com.ongi.api.ingredients.persistence.repository.RecipeIngredientRepository;
import com.ongi.ingredients.domain.Ingredient;
import com.ongi.ingredients.domain.IngredientNutrition;
import com.ongi.ingredients.domain.Nutrition;
import com.ongi.ingredients.domain.RecipeIngredient;
import com.ongi.ingredients.port.IngredientsRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class IngredientAdapter implements IngredientsRepositoryPort {

	private final IngredientRepository ingredientRepository;

	private final NutritionRepository nutritionRepository;

	private final IngredientNutritionRepository ingredientNutritionRepository;

	private final RecipeIngredientRepository recipeIngredientRepository;

	@Override
	public Ingredient save(Ingredient ingredient) {
		IngredientEntity entity = IngredientMapper.toEntity(ingredient);
		IngredientEntity saved = ingredientRepository.save(entity);
		return IngredientMapper.toDomain(saved);
	}

	@Override
	public Optional<Ingredient> findIngredientById(Long id) {
		return ingredientRepository
			.findById(id)
			.map(IngredientMapper::toDomain);
	}

	@Override
	public IngredientNutrition save(IngredientNutrition ingredientNutrition) {
		IngredientNutritionEntity entity = IngredientMapper.toEntity(ingredientNutrition);
		IngredientNutritionEntity saved = ingredientNutritionRepository.save(entity);
		return IngredientMapper.toDomain(saved);
	}

	@Override
	public Optional<IngredientNutrition> findIngredientNutritionById(Long id) {
		return ingredientNutritionRepository
			.findById(id)
			.map(IngredientMapper::toDomain);
	}

	@Override
	public Nutrition save(Nutrition nutrition) {
		NutritionEntity entity = IngredientMapper.toEntity(nutrition);
		NutritionEntity saved = nutritionRepository.save(entity);
		return IngredientMapper.toDomain(saved);
	}

	@Override
	public Optional<Nutrition> findNutritionById(Long id) {
		return nutritionRepository
			.findById(id)
			.map(IngredientMapper::toDomain);
	}

	@Override
	public RecipeIngredient save(RecipeIngredient recipeIngredient) {
		RecipeIngredientEntity entity = IngredientMapper.toEntity(recipeIngredient);
		RecipeIngredientEntity saved = recipeIngredientRepository.save(entity);
		return IngredientMapper.toDomain(saved);
	}

	@Override
	public Optional<RecipeIngredient> findRecipeIngredientById(Long id) {
		return recipeIngredientRepository
			.findById(id)
			.map(IngredientMapper::toDomain);
	}
}
