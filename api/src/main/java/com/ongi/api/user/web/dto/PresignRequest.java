package com.ongi.api.user.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PresignRequest(
	@NotBlank String contentType,
	@Min(1) @Max(5_000_000) long contentLength,
	String fileName
) {}
