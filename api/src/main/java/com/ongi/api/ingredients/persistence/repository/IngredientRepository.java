package com.ongi.api.ingredients.persistence.repository;

import com.ongi.api.ingredients.persistence.IngredientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<IngredientEntity, Long> {
}
