package com.ongi.api.community.web.dto;


public record AttachmentDto(
	Long id,
	String url,
	Integer width,
	Integer height,
	String mimeType
) {

}
