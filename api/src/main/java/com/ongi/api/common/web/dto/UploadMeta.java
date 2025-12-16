package com.ongi.api.common.web.dto;

public record UploadMeta(
	String objectKey,
	String contentType,
	long contentLength,
	String status
) {

}
