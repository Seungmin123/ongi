package com.ongi.api.user.web.dto;

public record EmailVerifyConfirmRequest(
	String email,
	String code
) {

}
