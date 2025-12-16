package com.ongi.api.user.web.dto;

import java.util.UUID;

public record PresignResponse(
	UUID uploadToken,
	String objectKey,
	String presignedUrl,
	long expiresInSeconds
) {}
