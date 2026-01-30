package com.ongi.api.community.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateAttachmentUploadUrlRequest(
	@NotBlank
	String fileName,
	@NotBlank
	String mimeType,
	@Positive
	long sizeBytes
) {

}
