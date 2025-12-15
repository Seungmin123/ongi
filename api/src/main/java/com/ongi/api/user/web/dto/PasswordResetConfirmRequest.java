package com.ongi.api.user.web.dto;

public record PasswordResetConfirmRequest(
	String token,
	String newPassword
) {

}
