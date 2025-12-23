package com.ongi.api.community.adatper.out.user;

public record UserSummary(
	Long userId,
	String displayName,
	String profileImageUrl
) {}
