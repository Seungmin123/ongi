package com.ongi.api.community.adatper.out.persistence.repository;

import com.ongi.api.community.adatper.out.persistence.CommunityCommentLikeEntity;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityCommentLikeRepository extends JpaRepository<CommunityCommentLikeEntity, Long> {

	@Query("select l.commentId from CommunityCommentLikeEntity l where l.userId = :userId and l.commentId in :commentIds")
	Set<Long> findLikedCommentIds(@Param("userId") Long userId, @Param("commentIds") Set<Long> commentIds);

	@Modifying
	@Query(
		value = """
      insert ignore into community_comment_like (user_id, comment_id)
      values (:userId, :commentId)
      """,
		nativeQuery = true
	)
	int insert(@Param("userId") long userId, @Param("commentId") long commentId);

	default boolean insertIfNotExists(long userId, long commentId) {
		return insert(userId, commentId) == 1;
	}

	@Modifying
	@Query("""
      delete from CommunityCommentLikeEntity ccl
      where ccl.userId = :userId and ccl.commentId = :commentId
    """)
	int deleteByUserIdAndCommentId(@Param("userId") long userId, @Param("commentId") long commentId);

	default boolean delete(long userId, long commentId) {
		return deleteByUserIdAndCommentId(userId, commentId) == 1;
	}
}
