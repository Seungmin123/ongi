package com.ongi.api.recipe.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import com.ongi.recipe.domain.enums.RecipeCommentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
	name = "recipe_comment",
	indexes = {
		@Index(name = "idx_rc_recipe_root_created_id", columnList = "recipe_id, root_id, created_at, recipe_comment_id"),
		@Index(name = "idx_rc_parent_id", columnList = "parent_id, recipe_comment_id"),
		@Index(name = "idx_rc_user_id_id", columnList = "user_id, recipe_comment_id")
	}
)
public class RecipeCommentEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recipe_comment_id", nullable = false)
	private Long id;

	@Column(name = "recipe_id", nullable = false)
	private Long recipeId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "content", columnDefinition = "TEXT", nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 30, nullable = false)
	private RecipeCommentStatus status;

	@Column(name = "root_id", nullable = false)
	private Long rootId;

	@Column(name = "parent_id")
	private Long parentId;

	@Column(name = "depth", nullable = false)
	private int depth;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Version
	private Long version;

	@Builder
	public RecipeCommentEntity(Long id, Long userId, Long recipeId, String content, Long rootId, Long parentId, int depth, RecipeCommentStatus status, LocalDateTime deletedAt, Long version) {
		this.id = id;
		this.recipeId = recipeId;
		this.userId = userId;
		this.content = content;
		this.rootId = rootId;
		this.parentId = parentId;
		this.depth = depth;
		this.status = status;
		this.deletedAt = deletedAt;
		this.version = version;
	}

	private RecipeCommentEntity(Long userId, Long recipeId, String content, Long rootId, Long parentId, int depth) {
		this.recipeId = recipeId;
		this.userId = userId;
		this.content = content;
		this.rootId = rootId;
		this.parentId = parentId;
		this.depth = depth;
		this.status = RecipeCommentStatus.ACTIVE;
	}

	public static RecipeCommentEntity createRoot(Long userId, Long recipeId, String content) {
		return new RecipeCommentEntity(userId, recipeId, content, 0L, null, 0);
	}

	public static RecipeCommentEntity createReply(Long userId, Long recipeId, String content, Long rootId, Long parentId, int depth) {
		return new RecipeCommentEntity(userId, recipeId, content, rootId, parentId, depth);
	}

	public void attachRootId(long rootId) {
		this.rootId = rootId;
	}

	public void updateContent(String content) {
		if (this.status != RecipeCommentStatus.ACTIVE) {
			throw new IllegalStateException("deleted comment");
		}
		this.content = content;
	}

	public boolean deleteSoft() {
		if (this.status != RecipeCommentStatus.ACTIVE) return false;
		this.status = RecipeCommentStatus.DELETED;
		this.deletedAt = LocalDateTime.now();
		// 정책: 삭제 시 상태 값만 바꾸고 내려줄 때 상태에 따라 삭제된 댓글로 변경
		// this.content = "삭제된 댓글";
		return true;
	}
}
