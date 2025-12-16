package com.ongi.api.user.web.dto;

import java.util.UUID;

public record MemberSignUpRequest(
	String email,
	String password,
	String displayName,
	UUID profileImageUploadToken,
	String profileImageObjectKey,
	String finalKey,
	String signUpToken
) {
}
