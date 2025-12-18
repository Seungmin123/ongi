package com.ongi.api.recipe.persistence.repository;

import com.ongi.api.recipe.persistence.RecipeCommentEntity;
import com.ongi.recipe.domain.enums.RecipeCommentStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeCommentRepository extends JpaRepository<RecipeCommentEntity, Long> {

	Optional<RecipeCommentEntity> findByIdAndRecipeId(Long id, Long recipeId);

	Optional<RecipeCommentEntity> findByIdAndRecipeIdAndStatus(Long id, Long recipeId, RecipeCommentStatus status);

}
