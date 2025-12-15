package com.ongi.api.user.web.dto;

public record MemberResponse(
	Long userId,
	String accessToken,
	String refreshToken
) {

}
