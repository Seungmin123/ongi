package com.ongi.api.ingredients.persistence;

import com.ongi.api.ingredients.persistence.repository.IngredientNutritionRepository;
import com.ongi.api.ingredients.persistence.repository.IngredientRepository;
import com.ongi.api.ingredients.persistence.repository.NutritionRepository;
import com.ongi.api.ingredients.persistence.repository.RecipeIngredientRepository;
import com.ongi.api.recipe.persistence.RecipeEntity;
import com.ongi.ingredients.domain.Ingredient;
import com.ongi.ingredients.domain.IngredientNutrition;
import com.ongi.ingredients.domain.Nutrition;
import com.ongi.ingredients.domain.RecipeIngredient;
import com.ongi.ingredients.domain.enums.IngredientCategoryEnum;
import com.ongi.ingredients.domain.enums.NutritionBasisEnum;
import com.ongi.ingredients.domain.enums.NutritionEnum;
import com.ongi.ingredients.port.IngredientsRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
		IngredientEntity ingredientRef = ingredientRepository.getReferenceById(ingredientNutrition.getIngredientId());
		NutritionEntity nutritionRef = nutritionRepository.getReferenceById(ingredientNutrition.getNutritionId());

		IngredientNutritionEntity entity = IngredientNutritionEntity.builder()
			.ingredient(ingredientRef)
			.nutrition(nutritionRef)
			.quantity(ingredientNutrition.getQuantity())
			.basis(ingredientNutrition.getBasis())
			.build();

		try {
			IngredientNutritionEntity saved = ingredientNutritionRepository.save(entity);
			return IngredientMapper.toDomain(saved);
		} catch (Exception e) {
			System.out.println(entity.getIngredient().getName());
			System.out.println(entity.getNutrition().getDisplayName());
			throw e;
		}

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
		IngredientEntity ingredientRef = ingredientRepository.getReferenceById(recipeIngredient.getIngredientId());
		RecipeIngredientEntity entity = RecipeIngredientEntity.builder()
			.id(recipeIngredient.getId())
			.recipeId(recipeIngredient.getRecipeId())
			.ingredient(ingredientRef)
			.quantity(recipeIngredient.getQuantity())
			.unit(recipeIngredient.getUnit())
			.note(recipeIngredient.getNote())
			.sortOrder(recipeIngredient.getSortOrder())
			.build();
		RecipeIngredientEntity saved = recipeIngredientRepository.save(entity);
		return IngredientMapper.toDomain(saved);
	}

	@Override
	public Optional<RecipeIngredient> findRecipeIngredientById(Long id) {
		return recipeIngredientRepository
			.findById(id)
			.map(IngredientMapper::toDomain);
	}

	@Override
	public List<RecipeIngredient> saveAll(List<RecipeIngredient> recipeIngredients) {
		List<RecipeIngredientEntity> entites = recipeIngredients.stream().map(IngredientMapper::toEntity).toList();
		List<RecipeIngredientEntity> saved = recipeIngredientRepository.saveAll(entites);
		return saved.stream().map(IngredientMapper::toDomain).toList();
	}

	@Override
	public Optional<Ingredient> findByName(String name) {
		return ingredientRepository.findByName(name)
			.map(IngredientMapper::toDomain);
	}

	@Override
	public Ingredient findIngredientByName(String name) {
		return ingredientRepository.findByName(name)
			.map(IngredientMapper::toDomain)
			.orElse(null);
	}

	@Override
	public Nutrition findNutritionByCode(NutritionEnum code) {
		return nutritionRepository.findByCode(code)
			.map(IngredientMapper::toDomain)
			.orElse(null);
	}

	// TODO Cache 관련 로직 추가
	@Override
	public Ingredient findOrCreateIngredient(String name, IngredientCategoryEnum ingredientCategory, Double caloriesKcal, Double proteinG, Double fatG, Double carbsG) {
		/*
		* // 캐시 우선
		if (ingredientCache.containsKey(name)) {
			return ingredientCache.get(name);
		}

		* ingredientCache.put(name, saved);
		* */
		return ingredientRepository.findByName(name)
			.map(IngredientMapper::toDomain)
			.orElseGet(() -> {
				IngredientEntity entity = IngredientEntity.builder()
					.name(name)
					.category(ingredientCategory)
					.caloriesKcal(defaultZero(caloriesKcal))
					.proteinG(defaultZero(proteinG))
					.fatG(defaultZero(fatG))
					.carbsG(defaultZero(carbsG))
					.build();
				IngredientEntity saved = ingredientRepository.save(entity);
				return IngredientMapper.toDomain(saved);
			});
	}

	private Double defaultZero(Double v) {
		return v == null ? 0d : v;
	}

	// TODO Cache 로직 추가
	public Nutrition findOrCreateNutrition(NutritionEnum code) {
		/*// 캐시 우선
		if (nutritionCache.containsKey(code)) {
			return nutritionCache.get(code);
		}
		nutritionCache.put(code, entity);

		*/
		return nutritionRepository.findByCode(code)
			.map(IngredientMapper::toDomain)
			.orElseGet(() -> {
				NutritionEntity entity = NutritionEntity.builder()
					.code(code)
					.unit(code.getUnit())
					.build();
				NutritionEntity saved = nutritionRepository.save(entity);
				return IngredientMapper.toDomain(saved);
			});
	}
}
