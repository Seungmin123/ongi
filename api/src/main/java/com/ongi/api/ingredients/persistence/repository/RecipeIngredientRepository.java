package com.ongi.api.ingredients.persistence.repository;

import com.ongi.api.ingredients.persistence.RecipeIngredientEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredientEntity, Long> {

	List<RecipeIngredientEntity> findByRecipeId(Long id);
}
