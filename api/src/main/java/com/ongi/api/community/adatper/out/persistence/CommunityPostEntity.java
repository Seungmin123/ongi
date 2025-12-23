package com.ongi.api.community.adatper.out.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import com.ongi.api.community.adatper.out.persistence.enums.PostStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="community_post",
	indexes = {
		@Index(name="idx_post_author_id_id", columnList="author_id, post_id"),
		@Index(name="idx_post_status_id", columnList="status, post_id"),
		@Index(name="idx_post_created_at_id", columnList="created_at, post_id")
	}
)
public class CommunityPostEntity extends BaseTimeEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="post_id")
	private Long id;

	@Column(name="author_id", nullable=false)
	private Long authorId;

	@Column(name="title", nullable=false, length=200)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(name="status", nullable=false, length=20)
	private PostStatus status; // ACTIVE, DELETED, HIDDEN

	@Column(name="content_schema", nullable=false)
	private String contentSchema;

	@Column(name="content_json", columnDefinition="JSON", nullable=false)
	private String contentJson; // canonical

	@Column(name="content_text", columnDefinition="TEXT", nullable=false)
	private String contentText; // 검색용 파생

	@Setter
	@Column(name="cover_attachment_id")
	private Long coverAttachmentId;

	@Column(name="deleted_at")
	private LocalDateTime deletedAt;

	@Version
	private Long version;

	public static CommunityPostEntity create(Long userId, String title, String schema, String contentJson, String contentText) {
		CommunityPostEntity entity = new CommunityPostEntity();
		entity.authorId = userId;
		entity.title = title;
		entity.contentJson = contentJson;
		entity.contentText = contentText;
		entity.contentSchema = schema;
		entity.status = PostStatus.ACTIVE;
		return entity;
	}

	public void update(String title, String schema, String json, String text) {
		if (status != PostStatus.ACTIVE) throw new IllegalStateException("post not active");
		this.title = title;
		this.contentSchema = schema;
		this.contentJson = json;
		this.contentText = text;
	}

	public boolean softDelete() {
		if (status == PostStatus.DELETED) return false;
		status = PostStatus.DELETED;
		deletedAt = LocalDateTime.now();
		return true;
	}

}
