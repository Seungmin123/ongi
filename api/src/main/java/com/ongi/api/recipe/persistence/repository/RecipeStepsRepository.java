package com.ongi.api.recipe.persistence.repository;

import com.ongi.api.recipe.persistence.RecipeStepsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeStepsRepository extends JpaRepository<RecipeStepsEntity, Long> {
}
