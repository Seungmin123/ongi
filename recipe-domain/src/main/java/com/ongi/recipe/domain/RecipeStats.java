package com.ongi.recipe.domain;

public class RecipeStats {

	private Long recipeId;

	private Long likeCount;

	private Long commentCount;

	private Long viewCount;

	private RecipeStats(Long recipeId, Long likeCount, Long commentCount, Long viewCount) {
		this.recipeId = recipeId;
		this.likeCount = likeCount;
		this.commentCount = commentCount;
		this.viewCount = viewCount;
	}

	public static RecipeStats create(Long recipeId, Long likeCount, Long commentCount, Long viewCount) {
		return new RecipeStats(recipeId, likeCount, commentCount, viewCount);
	}

	public Long getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(Long recipeId) {
		this.recipeId = recipeId;
	}

	public Long getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}

	public Long getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(Long commentCount) {
		this.commentCount = commentCount;
	}

	public Long getViewCount() {
		return viewCount;
	}

	public void setViewCount(Long viewCount) {
		this.viewCount = viewCount;
	}
}
