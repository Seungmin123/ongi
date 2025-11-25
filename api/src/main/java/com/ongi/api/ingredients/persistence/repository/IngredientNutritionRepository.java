package com.ongi.api.ingredients.persistence.repository;

import com.ongi.api.ingredients.persistence.IngredientNutritionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientNutritionRepository extends JpaRepository<IngredientNutritionEntity, Long> {
}
