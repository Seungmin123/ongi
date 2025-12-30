package com.ongi.api.ingredients.persistence.repository;

import com.ongi.api.ingredients.persistence.IngredientAllergenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientAllergenRepository extends JpaRepository<IngredientAllergenEntity, Long> {

}
