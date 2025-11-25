package com.ongi.api.ingredients.persistence.repository;

import com.ongi.api.ingredients.persistence.NutritionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionRepository extends JpaRepository<NutritionEntity, Long> {
}
