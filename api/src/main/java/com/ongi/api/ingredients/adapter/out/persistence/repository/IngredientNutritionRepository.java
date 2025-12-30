package com.ongi.api.ingredients.adapter.out.persistence.repository;

import com.ongi.api.ingredients.adapter.out.persistence.IngredientNutritionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientNutritionRepository extends JpaRepository<IngredientNutritionEntity, Long> {

	Optional<IngredientNutritionEntity> findByIngredientIdAndNutritionId(Long ingredientId, Long nutritionId);

}
