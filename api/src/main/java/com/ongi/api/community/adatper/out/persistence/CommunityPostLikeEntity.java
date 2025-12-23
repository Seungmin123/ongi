package com.ongi.api.community.adatper.out.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="community_post_like",
	uniqueConstraints = @UniqueConstraint(name="uk_post_like", columnNames={"post_id","user_id"}),
	indexes = {
		@Index(name="idx_post_like_post_id", columnList="post_id, created_at"),
		@Index(name="idx_post_like_user_id", columnList="user_id, created_at")
	}
)
public class CommunityPostLikeEntity extends BaseTimeEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="post_like_id")
	private Long id;

	@Column(name="post_id", nullable=false)
	private Long postId;

	@Column(name="user_id", nullable=false)
	private Long userId;

	@Builder
	public CommunityPostLikeEntity(Long id, Long postId, Long userId) {
		this.id = id;
		this.postId = postId;
		this.userId = userId;
	}
}
