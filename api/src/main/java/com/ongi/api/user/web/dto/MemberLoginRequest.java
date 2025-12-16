package com.ongi.api.user.web.dto;

public record MemberLoginRequest(
	String email,
	String password
) {

}
