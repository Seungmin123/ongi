package com.ongi.api.recipe.adapter.out.persistence.repository;

import com.ongi.api.recipe.adapter.out.persistence.RecipeTagsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeTagsRepository extends JpaRepository<RecipeTagsEntity, Long> {
}
