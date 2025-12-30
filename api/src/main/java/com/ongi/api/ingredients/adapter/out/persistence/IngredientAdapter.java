package com.ongi.api.ingredients.adapter.out.persistence;

import com.ongi.api.ingredients.adapter.out.persistence.repository.AllergenGroupRepository;
import com.ongi.api.ingredients.adapter.out.persistence.repository.IngredientNutritionRepository;
import com.ongi.api.ingredients.adapter.out.persistence.repository.IngredientRepository;
import com.ongi.api.ingredients.adapter.out.persistence.repository.NutritionRepository;
import com.ongi.api.ingredients.adapter.out.persistence.repository.RecipeIngredientRepository;
import com.ongi.ingredients.domain.AllergenGroup;
import com.ongi.ingredients.domain.Ingredient;
import com.ongi.ingredients.domain.IngredientNutrition;
import com.ongi.ingredients.domain.Nutrition;
import com.ongi.ingredients.domain.RecipeIngredient;
import com.ongi.ingredients.domain.enums.IngredientCategoryEnum;
import com.ongi.ingredients.domain.enums.NutritionEnum;
import com.ongi.ingredients.port.IngredientsRepositoryPort;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// TODO Root Entity 만 저장 조회, Mapping Entity는 외부에서 영속성을 주입받아 getReferenceById 통해 저장 할 것.
// TODO Root 수정 시 ifElse 분기 처리를 통해 FindById 후 수정
// TODO Mapping 수정 시 ifElse 분기 통해 Create 는 getReferenceById, Update 는 findById 후 수정된 값만 변경
@RequiredArgsConstructor
@Repository
public class IngredientAdapter implements IngredientsRepositoryPort {

	private final IngredientRepository ingredientRepository;

	private final NutritionRepository nutritionRepository;

	private final IngredientNutritionRepository ingredientNutritionRepository;

	private final RecipeIngredientRepository recipeIngredientRepository;

	private final AllergenGroupRepository allergenGroupRepository;

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
	public Set<Ingredient> findIngredientsByIds(Collection<Long> ingredientIds) {
		return ingredientRepository.findAllById(ingredientIds).stream()
			.map(IngredientMapper::toDomain)
			.collect(Collectors.toSet());
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
		IngredientNutritionEntity saved = ingredientNutritionRepository.save(entity);
		return IngredientMapper.toDomain(saved);
	}

	@Override
	public List<IngredientNutrition> saveAllIngredientNutrions(List<IngredientNutrition> ingredientNutritions) {
		// 1) 배치 내부 중복 제거 (ingredientId + nutritionId 기준)
		Map<String, IngredientNutrition> deduped = new LinkedHashMap<>();

		for (IngredientNutrition domain : ingredientNutritions) {
			Long ingredientId = domain.getIngredientId();
			Long nutritionId  = domain.getNutritionId();

			if (ingredientId == null) {
				throw new IllegalStateException("Ingredient id is null");
			}
			if (nutritionId == null) {
				throw new IllegalStateException("Nutrition id is null");
			}

			String key = ingredientId + "-" + nutritionId;

			// 정책 선택:
			// 1) 최초 값 유지: deduped.putIfAbsent(key, domain);
			// 2) 마지막 값으로 덮어쓰기: deduped.put(key, domain);
			deduped.merge(key, domain, (oldVal, newVal) -> {
				if(oldVal.getQuantity() < newVal.getQuantity()) {
					return oldVal;
				} else
					return newVal;
			});
		}

		// 2) 이제 각 (ingredientId, nutritionId) 조합은 배치 내에서 딱 1개씩만 존재
		List<IngredientNutritionEntity> entities = deduped.values().stream()
			.map(domain -> {
				Long ingredientId = domain.getIngredientId();
				Long nutritionId  = domain.getNutritionId();

				// TODO 검색 후 없을 경우 Insert -> 추후 부활
				/*return ingredientNutritionRepository.findByIngredientIdAndNutritionId(ingredientId, nutritionId)
					.map(existing -> {
						existing.setQuantity(domain.getQuantity());
						existing.setBasis(domain.getBasis());
						return existing;
					})
					.orElseGet(() -> {
						IngredientEntity ingredientRef = ingredientRepository.getReferenceById(ingredientId);
						NutritionEntity nutritionRef   = nutritionRepository.getReferenceById(nutritionId);
						return IngredientMapper.toEntity(domain, ingredientRef, nutritionRef);
					});*/

				IngredientEntity ingredientRef = ingredientRepository.getReferenceById(ingredientId);
				NutritionEntity nutritionRef   = nutritionRepository.getReferenceById(nutritionId);
				return IngredientMapper.toEntity(domain, ingredientRef, nutritionRef);
			})
			.toList();

		List<IngredientNutritionEntity> saved = ingredientNutritionRepository.saveAll(entities);
		return saved.stream().map(IngredientMapper::toDomain).toList();
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
	public List<Nutrition> saveAllNutritions(List<Nutrition> nutritions) {
		List<NutritionEntity> entites = nutritions.stream().map(IngredientMapper::toEntity).toList();
		List<NutritionEntity> saved = nutritionRepository.saveAll(entites);
		return saved.stream().map(IngredientMapper::toDomain).toList();
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
	public List<RecipeIngredient> findRecipeIngredientByRecipeId(Long recipeId) {
		return recipeIngredientRepository
			.findByRecipeId(recipeId)
			.stream()
			.map(IngredientMapper::toDomain)
			.toList();
	}

	@Override
	public List<RecipeIngredient> saveAllRecipeIngredients(List<RecipeIngredient> recipeIngredients) {
		List<RecipeIngredientEntity> entites = recipeIngredients.stream().map(domain -> {
			Long ingredientId = domain.getIngredientId();

			if(ingredientId == null) {
				// TODO Custom
				throw new IllegalStateException("Ingredient id is null");
			}

			IngredientEntity ingredientRef = ingredientRepository.getReferenceById(ingredientId);

			return IngredientMapper.toEntity(domain, ingredientRef);
		}).toList();
		List<RecipeIngredientEntity> saved = recipeIngredientRepository.saveAll(entites);
		return saved.stream().map(IngredientMapper::toDomain).toList();
	}

	@Override
	public void deleteRecipeIngredientByRecipeId(Long recipeId) {
		recipeIngredientRepository.deleteByRecipeId(recipeId);
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

	@Override
	public Set<AllergenGroup> findAllergenGroupsByIds(Collection<Long> allergenGroupIds) {
		return allergenGroupRepository.findAllById(allergenGroupIds).stream()
			.map(IngredientMapper::toDomain)
			.collect(Collectors.toSet());
	}

	// TODO Cache 관련 로직 추가
	@Override
	public Ingredient findOrCreateIngredient(String name, String code, IngredientCategoryEnum ingredientCategory, Double caloriesKcal, Double proteinG, Double fatG, Double carbsG) {
		/*
		* // 캐시 우선
		if (ingredientCache.containsKey(name)) {
			return ingredientCache.get(name);
		}

		* ingredientCache.put(name, saved);
		* */

		// TODO 검색 후 없을 경우 Insert -> 부활
		/*return ingredientRepository.findByName(name)
			.map(IngredientMapper::toDomain)
			.orElseGet(() -> {
				IngredientEntity entity = IngredientEntity.builder()
					.name(name)
					.code(code)
					.category(ingredientCategory)
					.caloriesKcal(defaultZero(caloriesKcal))
					.proteinG(defaultZero(proteinG))
					.fatG(defaultZero(fatG))
					.carbsG(defaultZero(carbsG))
					.build();
				IngredientEntity saved = ingredientRepository.save(entity);
				return IngredientMapper.toDomain(saved);
			});*/

		IngredientEntity entity = IngredientEntity.builder()
			.name(name)
			.code(code)
			.category(ingredientCategory)
			.caloriesKcal(defaultZero(caloriesKcal))
			.proteinG(defaultZero(proteinG))
			.fatG(defaultZero(fatG))
			.carbsG(defaultZero(carbsG))
			.build();
		IngredientEntity saved = ingredientRepository.save(entity);
		return IngredientMapper.toDomain(saved);
	}

	@Override
	public Ingredient findLikeOrCreateIngredient(String name, IngredientCategoryEnum ingredientCategory, Double caloriesKcal, Double proteinG, Double fatG, Double carbsG) {
		/*
		* // 캐시 우선
		if (ingredientCache.containsKey(name)) {
			return ingredientCache.get(name);
		}

		* ingredientCache.put(name, saved);
		* */
		return ingredientRepository.findFirstByNameOrderByNameAsc(name)
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
		// TODO 검색 후 없을 경우 Insert -> 추후 부활
		/*return nutritionRepository.findByCode(code)
			.map(IngredientMapper::toDomain)
			.orElseGet(() -> {
				NutritionEntity entity = NutritionEntity.builder()
					.code(code)
					.unit(code.getUnit())
					.build();
				NutritionEntity saved = nutritionRepository.save(entity);
				return IngredientMapper.toDomain(saved);
			});*/

		NutritionEntity entity = NutritionEntity.builder()
			.code(code)
			.unit(code.getUnit())
			.build();
		NutritionEntity saved = nutritionRepository.save(entity);
		return IngredientMapper.toDomain(saved);
	}
}
