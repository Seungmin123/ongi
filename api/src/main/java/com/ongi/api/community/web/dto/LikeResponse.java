package com.ongi.api.community.web.dto;

public record LikeResponse(
	boolean likedByMe,
	long likeCount
) {}