package com.ongi.user.domain;

public class UserStats {

	private Long userId;

	private Long uploadedRecipeCount;

	private Long savedRecipeCount;

	private Long myRecipeTotalViewCount;

	private Long myPostCount;

	private Long myCommentCount;

	private UserStats(
		Long userId,
		Long uploadedRecipeCount,
		Long savedRecipeCount,
		Long myRecipeTotalViewCount,
		Long myPostCount,
		Long myCommentCount
	) {
		this.userId = userId;
		this.uploadedRecipeCount = uploadedRecipeCount;
		this.savedRecipeCount = savedRecipeCount;
		this.myRecipeTotalViewCount = myRecipeTotalViewCount;
		this.myPostCount = myPostCount;
		this.myCommentCount = myCommentCount;
	}

	public static UserStats create(
		Long userId, Long uploadedRecipeCount, Long savedRecipeCount, Long myRecipeTotalViewCount, Long myPostCount, Long myCommentCount
	) {
		return new UserStats(userId, uploadedRecipeCount, savedRecipeCount, myRecipeTotalViewCount, myPostCount, myCommentCount);
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getUploadedRecipeCount() {
		return uploadedRecipeCount;
	}

	public void setUploadedRecipeCount(Long uploadedRecipeCount) {
		this.uploadedRecipeCount = uploadedRecipeCount;
	}

	public Long getSavedRecipeCount() {
		return savedRecipeCount;
	}

	public void setSavedRecipeCount(Long savedRecipeCount) {
		this.savedRecipeCount = savedRecipeCount;
	}

	public Long getMyRecipeTotalViewCount() {
		return myRecipeTotalViewCount;
	}

	public void setMyRecipeTotalViewCount(Long myRecipeTotalViewCount) {
		this.myRecipeTotalViewCount = myRecipeTotalViewCount;
	}

	public Long getMyPostCount() {
		return myPostCount;
	}

	public void setMyPostCount(Long myPostCount) {
		this.myPostCount = myPostCount;
	}

	public Long getMyCommentCount() {
		return myCommentCount;
	}

	public void setMyCommentCount(Long myCommentCount) {
		this.myCommentCount = myCommentCount;
	}
}
