package com.ongi.api.community.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpsertRequest (
	Long postId,
	@NotBlank @Size(min = 1, max = 100)
	String title,
	String schema,
	@NotBlank
	String contentJson
){ }
