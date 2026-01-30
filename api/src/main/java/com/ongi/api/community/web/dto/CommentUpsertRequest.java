package com.ongi.api.community.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentUpsertRequest(
	Long parentId,
	String schema,
	@NotBlank
	String contentJson
){ }
