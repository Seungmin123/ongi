package com.ongi.api.user.web.dto;

import com.ongi.user.domain.enums.UserEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record UserEventDto(
	@NotBlank
	@Size(max = 100)
	String eventId,              // 클라 UUID (dedup key)

	@NotNull
	UserEventType type,

	@NotBlank
	@Size(max = 50)
	String occurredAt,

	@NotBlank
	@Size(max = 50)
	String pageKey,              // "recipe_detail" 등

	Long recipeId,

	@Size(max = 200)
	String referrer,

	Map<String, Object> props    // dwellMsDelta, maxScrollRatio 등
) {}
