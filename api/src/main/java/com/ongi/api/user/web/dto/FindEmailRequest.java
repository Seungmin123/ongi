package com.ongi.api.user.web.dto;

public record FindEmailRequest(
	String email,
	String displayName
) {

}
