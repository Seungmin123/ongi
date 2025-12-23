package com.ongi.api.community.web.dto;

public record PostAttachmentRow(
	long id,
	String url,
	Integer width,
	Integer height,
	String mimType
) {

}
