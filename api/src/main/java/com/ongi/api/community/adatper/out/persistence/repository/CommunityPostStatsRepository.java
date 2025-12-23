package com.ongi.api.community.adatper.out.persistence.repository;

import com.ongi.api.community.adatper.out.persistence.CommunityPostStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostStatsRepository extends JpaRepository<CommunityPostStatsEntity, Long> {

	@Modifying
	@Query("""
        update CommunityPostStatsEntity s
        set s.viewCount = s.viewCount + :delta
        where s.postId = :postId
    """)
	void incrementViewCount(@Param("postId") long postId, @Param("delta") long delta);

	@Modifying
	@Query("""
      update CommunityPostStatsEntity r
      set r.likeCount = r.likeCount + :delta
      where r.postId = :postId
    """)
	void incrementLikeCount(@Param("postId") long postId, @Param("delta") long delta);

	@Query("""
      select r.likeCount from CommunityPostStatsEntity r where r.postId = :postId
    """)
	long findLikeCount(@Param("postId") long postId);

	@Modifying
	@Query("""
      update CommunityPostStatsEntity r
      set r.commentCount = r.commentCount + :delta
      where r.postId = :postId
    """)
	void incrementCommentCount(@Param("postId") long postId, @Param("delta") long delta);

	@Query("""
      select r.commentCount from CommunityPostStatsEntity r where r.postId = :postId
    """)
	long findCommentCount(@Param("postId") long postId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
	insert into community_post_stats (post_id, comment_count, like_count, view_count, created_at, modified_at)
	values (:postId, :delta, 0, 0, now(), now())
	on duplicate key update
	  comment_count = greatest(comment_count + :delta, 0),
	  modified_at = now()
	""", nativeQuery = true)
	int upsertIncCommentCount(@Param("postId") long postId, @Param("delta") long delta);

}
