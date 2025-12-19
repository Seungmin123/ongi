package com.ongi.api.recipe.adapter.out.persistence.repository;

import com.ongi.api.recipe.adapter.out.persistence.RecipeBookmarkEntity;
import com.ongi.api.recipe.adapter.out.persistence.RecipeBookmarkId;
import com.ongi.api.recipe.adapter.out.persistence.RecipeLikeEntity;
import com.ongi.api.recipe.adapter.out.persistence.RecipeLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeBookmarkRepository extends JpaRepository<RecipeBookmarkEntity, RecipeBookmarkId> {

	// INSERT IGNORE / ON CONFLICT DO NOTHING 패턴
	@Modifying
	@Query(
		value = """
      insert ignore into recipe_bookmark (user_id, recipe_id)
      values (:userId, :recipeId)
      """,
		nativeQuery = true
	)
	int insert(@Param("userId") long userId, @Param("recipeId") long recipeId);

	default boolean insertIfNotExists(long userId, long recipeId) {
		return insert(userId, recipeId) == 1;
	}

	@Modifying
	@Query("""
      delete from RecipeBookmarkEntity rl
      where rl.id.userId = :userId and rl.id.recipeId = :recipeId
    """)
	int deleteByRecipeIdAndUserId(@Param("userId") long userId, @Param("recipeId") long recipeId);

	boolean existsByUserIdAndRecipeId(long userId, long recipeId);
}
