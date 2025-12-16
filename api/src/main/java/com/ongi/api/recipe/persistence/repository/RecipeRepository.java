package com.ongi.api.recipe.persistence.repository;

import com.ongi.api.recipe.persistence.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeRepository extends JpaRepository<RecipeEntity, Long> {

	@Modifying
	@Query("""
      update RecipeEntity r
      set r.likeCount = r.likeCount + :delta
      where r.id = :recipeId
    """)
	void incrementLikeCount(@Param("recipeId") long recipeId,
		@Param("delta") long delta);

	@Query("""
      select r.likeCount from RecipeEntity r where r.id = :recipeId
    """)
	long findLikeCount(@Param("recipeId") long recipeId);

}
