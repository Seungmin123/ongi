package com.ongi.api.ingredients.persistence.repository;

import com.ongi.api.ingredients.persistence.RecipeIngredientEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredientEntity, Long> {

	@EntityGraph(attributePaths = {"ingredient"})
	List<RecipeIngredientEntity> findByRecipeId(Long id);

	void deleteByRecipeId(Long id);
}
