package com.ongi.api.community.adatper.out.persistence.repository;

import com.ongi.api.community.adatper.out.persistence.CommunityPostLikeEntity;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLikeEntity, Long> {

	@Modifying
	@Query(
		value = """
      insert ignore into community_post_like (user_id, post_id)
      values (:userId, :postId)
      """,
		nativeQuery = true
	)
	int insert(@Param("userId") long userId, @Param("postId") long postId);

	default boolean insertIfNotExists(long userId, long postId) {
		return insert(userId, postId) == 1;
	}

	@Modifying
	@Query("""
      delete from CommunityPostLikeEntity cpl
      where cpl.userId = :userId and cpl.postId = :postId
    """)
	int deleteByUserIdAndPostId(@Param("userId") long userId, @Param("postId") long postId);

	default boolean delete(long userId, long postId) {
		return deleteByUserIdAndPostId(userId, postId) == 1;
	}

	@Query("select l.postId from CommunityPostLikeEntity l where l.userId = :userId and l.postId in :postIds")
	Set<Long> findLikedPostIds(@Param("userId") Long userId, @Param("postIds") Set<Long> postIds);

}
