package com.ongi.api.user.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FindEmailRequest(
	@NotBlank @Email
	String email,
	@NotBlank
	String displayName
) {

}
