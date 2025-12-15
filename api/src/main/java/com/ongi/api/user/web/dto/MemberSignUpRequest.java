package com.ongi.api.user.web.dto;

public record MemberSignUpRequest(
	String email,
	String password,
	String displayName,
	String profileImageUrl,
	String signUpToken
) {

}
