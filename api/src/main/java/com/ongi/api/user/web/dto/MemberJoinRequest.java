package com.ongi.api.user.web.dto;

public record MemberJoinRequest(
	String email,
	String password,
	String displayName,
	String profileImageUrl
) {

}
