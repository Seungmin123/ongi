package com.ongi.api.recipe.persistence.repository;

import com.ongi.api.recipe.persistence.RecipeTagsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeTagsRepository extends JpaRepository<RecipeTagsEntity, Long> {
}
