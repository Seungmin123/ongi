package com.ongi.api.recipe.adapter.out.persistence.repository;

import com.ongi.api.recipe.adapter.out.persistence.RecipeStatsEntity;
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
	void incrementLikeCount(@Param("recipeId") long recipeId, @Param("delta") long delta);

	@Query("""
      select r.likeCount from RecipeStatsEntity r where r.recipeId = :recipeId
    """)
	long findLikeCount(@Param("recipeId") long recipeId);

	@Modifying
	@Query("""
      update RecipeStatsEntity r
      set r.bookmarkCount = r.bookmarkCount + :delta
      where r.recipeId = :recipeId
    """)
	void incrementBookmarkCount(@Param("recipeId") long recipeId, @Param("delta") long delta);

	@Query("""
      select r.bookmarkCount from RecipeStatsEntity r where r.recipeId = :recipeId
    """)
	long findBookmarkCount(@Param("recipeId") long recipeId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
	insert into recipe_stats (recipe_id, comment_count, like_count, view_count, created_at, modified_at)
	values (:recipeId, :delta, 0, 0, now(), now())
	on duplicate key update
	  comment_count = greatest(comment_count + :delta, 0),
	  modified_at = now()
	""", nativeQuery = true)
	int upsertIncCommentCount(@Param("recipeId") long recipeId, @Param("delta") long delta);

	@Query("""
      select r.commentCount from RecipeStatsEntity r where r.recipeId = :recipeId
    """)
	long findCommentCount(@Param("recipeId") long recipeId);

}
