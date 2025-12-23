package com.ongi.api.community.web.dto;

public record CreateAttachmentUploadUrlRequest(
	String fileName,
	String mimeType,
	long sizeBytes
) {

}
