package com.ongi.api.recipe.persistence.repository;

import com.ongi.api.recipe.persistence.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<RecipeEntity, Long> {

}
