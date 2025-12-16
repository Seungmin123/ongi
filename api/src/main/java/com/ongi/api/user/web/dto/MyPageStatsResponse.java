package com.ongi.api.user.web.dto;


public record MyPageStatsResponse(
	Long uploadedRecipeCount,
	Long savedRecipeCount,
	Long myRecipeTotalViewCount,
	Long myPostCount,
	Long myCommentCount
) {
}
