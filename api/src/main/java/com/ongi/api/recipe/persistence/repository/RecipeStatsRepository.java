package com.ongi.api.recipe.persistence.repository;

import com.ongi.api.recipe.persistence.RecipeStatsEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeStatsRepository extends JpaRepository<RecipeStatsEntity, Long> {

	@Modifying
	@Query("""
        update RecipeStatsEntity s
        set s.viewCount = s.viewCount + :delta
        where s.recipeId = :recipeId
    """)
	void incrementViewCount(@Param("recipeId") long recipeId, @Param("delta") long delta);

	@Modifying
	@Query("""
      update RecipeStatsEntity r
      set r.likeCount = r.likeCount + :delta
      where r.recipeId = :recipeId
    """)
	void incrementLikeCount(@Param("recipeId") long recipeId,
		@Param("delta") long delta);

	@Query("""
      select r.likeCount from RecipeStatsEntity r where r.recipeId = :recipeId
    """)
	long findLikeCount(@Param("recipeId") long recipeId);

	@Modifying
	@Query(value = """
        update recipe_stats
        set like_count = like_count + :delta
        where recipe_id = :recipeId
        """, nativeQuery = true)
	int incrementLikedCount(@Param("recipeId") long recipeId, @Param("delta") long delta);

	/*@Modifying
	@Query(value = """
        update recipe_stats
        set saved_recipe_count = saved_recipe_count + :delta
        where user_id = :userId
        """, nativeQuery = true)
	int incrementSavedCount(@Param("userId") long userId, @Param("delta") long delta);*/
}
