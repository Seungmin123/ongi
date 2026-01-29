package com.ongi.api.community.web.dto;

import jakarta.validation.constraints.NotBlank;

public record JwtRefreshRequest (
	@NotBlank
	String refreshToken
) {

}
