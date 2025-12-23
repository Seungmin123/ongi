package com.ongi.api.community.adatper.out.persistence.repository;

import com.ongi.api.community.adatper.out.persistence.CommunityPostEntity;
import com.ongi.api.community.adatper.out.persistence.enums.PostStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPostEntity, Long> {

	Optional<CommunityPostEntity> findByIdAndStatus(Long postId, PostStatus status);

	boolean existsByIdAndStatus(Long postId, PostStatus status);
}
