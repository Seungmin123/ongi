package com.ongi.api.community.web.dto;

public record CreateAttachmentUploadUrlResponse(
	String storageKey,
	String uploadUrl,
	long maxBytes,
	long expiresInSeconds
) {

}
