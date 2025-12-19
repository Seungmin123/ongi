package com.ongi.api.recipe.adapter.out.persistence.repository;

import com.ongi.api.recipe.adapter.out.persistence.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<RecipeEntity, Long> {

}
