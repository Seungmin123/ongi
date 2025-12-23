package com.ongi.api.community.adatper.out.persistence.repository;

import com.ongi.api.community.adatper.out.persistence.CommunityCommentEntity;
import com.ongi.api.community.adatper.out.persistence.enums.CommentStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityCommentRepository extends JpaRepository<CommunityCommentEntity, Long> {

	Page<CommunityCommentEntity> findByPostIdAndStatusIn(
		Long postId, List<CommentStatus> statuses, Pageable pageable
	);

	@Query("select c from CommunityCommentEntity c where c.postId=:postId")
	Page<CommunityCommentEntity> findByPostId(Long postId, Pageable pageable);

	@Modifying
	@Query("""
      update CommunityCommentEntity r
      set r.likeCount = r.likeCount + :delta
      where r.postId = :postId
      and r.id = :commentId
    """)
	void incrementLikeCount(@Param("postId") long postId, @Param("commentId") long commentId, @Param("delta") long delta);

	@Query("""
      select r.likeCount from CommunityCommentEntity r where r.postId = :postId and r.id = :commentId
    """)
	long findLikeCount(@Param("postId") long postId, @Param("commentId") long commentId);
}
