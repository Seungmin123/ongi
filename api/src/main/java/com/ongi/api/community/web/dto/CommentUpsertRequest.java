package com.ongi.api.community.web.dto;

public record CommentUpsertRequest(
	Long parentId,
	String schema,
	String contentJson
){ }
