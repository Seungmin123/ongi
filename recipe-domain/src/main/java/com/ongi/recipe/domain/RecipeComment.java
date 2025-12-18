package com.ongi.recipe.domain;

import com.ongi.recipe.domain.enums.RecipeCommentStatus;
import java.time.LocalDateTime;

public class RecipeComment {

	private Long id;

	private Long recipeId;

	private Long userId;

	private String content;

	private RecipeCommentStatus status;

	private Long parentId;

	private int depth;

	private LocalDateTime deletedAt;

	private Long version;

	private RecipeComment(Long id, Long userId, Long recipeId, String content,
		RecipeCommentStatus status, Long parentId, int depth, LocalDateTime deletedAt,
		Long version) {
		this.id = id;
		this.recipeId = recipeId;
		this.userId = userId;
		this.content = content;
		this.status = status;
		this.parentId = parentId;
		this.depth = depth;
		this.deletedAt = deletedAt;
		this.version = version;
	}

	public static RecipeComment create(
		Long id, Long userId, Long recipeId, String content,
		RecipeCommentStatus status, Long parentId, int depth, LocalDateTime deletedAt,
		Long version
	) {
		return new RecipeComment(id, userId, recipeId, content, status, parentId, depth, deletedAt, version);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(Long recipeId) {
		this.recipeId = recipeId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public RecipeCommentStatus getStatus() {
		return status;
	}

	public void setStatus(RecipeCommentStatus status) {
		this.status = status;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
