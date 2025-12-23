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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="community_comment_like",
	uniqueConstraints = @UniqueConstraint(name="uk_comment_like", columnNames={"comment_id","user_id"}),
	indexes = {
		@Index(name="idx_comment_like_comment_id", columnList="comment_id, created_at"),
		@Index(name="idx_comment_like_user_id", columnList="user_id, created_at")
	}
)
public class CommunityCommentLikeEntity extends BaseTimeEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="comment_like_id")
	private Long id;

	@Column(name="comment_id", nullable=false)
	private Long commentId;

	@Column(name="user_id", nullable=false)
	private Long userId;

	public static CommunityCommentLikeEntity of(Long commentId, Long userId) {
		CommunityCommentLikeEntity e = new CommunityCommentLikeEntity();
		e.commentId = commentId;
		e.userId = userId;
		return e;
	}

}
