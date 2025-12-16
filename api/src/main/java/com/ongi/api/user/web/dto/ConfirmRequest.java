package com.ongi.api.user.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ConfirmRequest(
	@NotNull UUID uploadToken,
	@NotBlank String objectKey
) {}
