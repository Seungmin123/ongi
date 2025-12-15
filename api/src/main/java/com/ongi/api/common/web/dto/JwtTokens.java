package com.ongi.api.common.web.dto;

public record JwtTokens(
	String accessToken,
	String refreshToken
) {

}
