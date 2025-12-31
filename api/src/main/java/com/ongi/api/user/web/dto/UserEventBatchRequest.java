package com.ongi.api.user.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UserEventBatchRequest(
	@NotBlank
	@Size(max = 100)
	String sessionId,

	// 비로그인/디바이스 기반까지 염두면 받는게 좋음(없으면 null 가능)
	@Size(max = 200)
	String deviceId,

	// 클라이언트가 가진 현재 시각(디버깅/오차 추적용) - 없어도 됨
	@Size(max = 50)
	String clientTs,

	@NotEmpty
	List<@Valid UserEventDto> events
) {}
