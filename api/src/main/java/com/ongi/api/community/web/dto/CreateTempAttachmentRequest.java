package com.ongi.api.community.web.dto;

public record CreateTempAttachmentRequest(
	String storageKey,
	String mimeType,
	long sizeBytes,
	Integer width,
	Integer height
) {

}
