package com.ongi.api.community.adatper.out.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="community_post_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPostStatsEntity extends BaseTimeEntity {

	@Id
	@Column(name="post_id")
	private Long postId;

	@Column(name="like_count", nullable=false)
	private long likeCount;

	@Column(name="comment_count", nullable=false)
	private long commentCount;

	@Column(name="view_count", nullable=false)
	private long viewCount;

	@Version
	private Long version;

	public static CommunityPostStatsEntity create(Long postId) {
		CommunityPostStatsEntity entity = new CommunityPostStatsEntity();
		entity.postId = postId;
		entity.likeCount = 0L;
		entity.commentCount = 0L;
		entity.viewCount = 0L;
		return entity;
	}
}
