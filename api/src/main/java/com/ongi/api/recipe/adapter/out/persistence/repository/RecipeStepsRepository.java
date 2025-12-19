package com.ongi.api.recipe.adapter.out.persistence.repository;

import com.ongi.api.recipe.adapter.out.persistence.RecipeStepsEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeStepsRepository extends JpaRepository<RecipeStepsEntity, Long> {

	List<RecipeStepsEntity> findRecipeStepsByRecipeId(Long id);

	void deleteByRecipeId(Long id);

	void deleteAllById(Long id);
}
