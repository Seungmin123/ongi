package com.ongi.api.community.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateTempAttachmentRequest(
	@NotBlank
	String storageKey,
	@NotBlank
	String mimeType,
	@Positive
	long sizeBytes,
	Integer width,
	Integer height
) {

}
