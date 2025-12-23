package com.ongi.api.community.adatper.out.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import com.ongi.api.community.adatper.out.persistence.enums.CommentStatus;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="community_comment",
	indexes = {
		@Index(name="idx_comment_post_root_created", columnList="post_id, root_id, created_at, comment_id"),
		@Index(name="idx_comment_post_id", columnList="post_id, comment_id"),
		@Index(name="idx_comment_author_id_id", columnList="author_id, comment_id"),
		@Index(name="idx_comment_parent_id_id", columnList="parent_id, comment_id"),
		@Index(name="idx_comment_root_id_id", columnList="root_id, comment_id")
	}
)
public class CommunityCommentEntity extends BaseTimeEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="comment_id")
	private Long id;

	@Column(name="post_id", nullable=false)
	private Long postId;

	@Column(name="author_id", nullable=false)
	private Long authorId;

	@Column(name="root_id", nullable=false)
	private Long rootId;      // root thread id (root comment id)

	@Column(name="parent_id")
	private Long parentId;    // null for root

	@Column(name="depth", nullable=false)
	private int depth;        // 0 for root, parent.depth+1 for reply

	@Enumerated(EnumType.STRING)
	@Column(name="status", nullable=false, length=20)
	private CommentStatus status; // ACTIVE, DELETED

	@Column(name="content_schema", nullable=false)
	private String contentSchema;

	@Column(name="content_json", columnDefinition="JSON", nullable=false)
	private String contentJson;

	@Column(name="content_text", columnDefinition="TEXT", nullable=false)
	private String contentText;

	@ColumnDefault("0")
	@Column(name="like_count")
	private long likeCount;

	@Column(name="deleted_at")
	private LocalDateTime deletedAt;

	@Version
	private Long version;

	public static CommunityCommentEntity createRoot(Long postId, Long authorId, String schema, String json, String text) {
		CommunityCommentEntity e = new CommunityCommentEntity();
		e.postId = postId;
		e.authorId = authorId;
		e.parentId = null;
		e.depth = 0;
		e.status = CommentStatus.ACTIVE;
		e.contentSchema = schema;
		e.contentJson = json;
		e.contentText = text;
		// rootId는 save 후 id로 채움
		e.rootId = 0L;
		e.likeCount = 0L;
		return e;
	}

	public static CommunityCommentEntity createReply(Long postId, Long authorId,
		Long parentId, Long rootId, int depth, String schema, String json, String text) {
		CommunityCommentEntity e = new CommunityCommentEntity();
		e.postId = postId;
		e.authorId = authorId;
		e.parentId = parentId;
		e.rootId = rootId;
		e.depth = depth;
		e.status = CommentStatus.ACTIVE;
		e.contentSchema = schema;
		e.contentJson = json;
		e.contentText = text;
		e.rootId = 0L;
		return e;
	}

	public void attachRootId(Long rootId) {
		this.rootId = rootId;
	}

	public void update(String schema, String json, String text) {
		if (status != CommentStatus.ACTIVE) throw new IllegalStateException("deleted comment");
		this.contentSchema = schema;
		this.contentJson = json;
		this.contentText = text;
	}

	public boolean softDelete() {
		if (status != CommentStatus.ACTIVE) return false;
		status = CommentStatus.DELETED;
		deletedAt = LocalDateTime.now();
		return true;
	}

}
