package com.ongi.api.recipe.persistence.repository;

import com.ongi.api.recipe.persistence.RecipeLikeEntity;
import com.ongi.api.recipe.persistence.RecipeLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeLikeRepository extends JpaRepository<RecipeLikeEntity, RecipeLikeId> {

	// INSERT IGNORE / ON CONFLICT DO NOTHING 패턴
	@Modifying
	@Query(
		value = """
      insert ignore into recipe_like (recipe_id, user_id)
      values (:recipeId, :userId)
      """,
		nativeQuery = true
	)
	int insert(@Param("recipeId") long recipeId, @Param("userId") long userId);

	default boolean insertIfNotExists(long recipeId, long userId) {
		return insert(recipeId, userId) == 1;
	}

	@Modifying
	@Query("""
      delete from RecipeLikeEntity rl
      where rl.id.recipeId = :recipeId and rl.id.userId = :userId
    """)
	int deleteByRecipeIdAndUserId(@Param("recipeId") long recipeId,
		@Param("userId") long userId);

}
