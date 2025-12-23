package com.ongi.api.community.web.dto;

public record PostUpsertRequest (
	Long postId,
	String title,
	String schema,
	String contentJson
){ }
