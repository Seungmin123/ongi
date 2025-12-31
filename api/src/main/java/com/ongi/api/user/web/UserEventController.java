package com.ongi.api.user.web;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.common.web.dto.AuthPrincipal;
import com.ongi.api.user.application.event.UserEventService;
import com.ongi.api.user.web.dto.UserEventBatchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/user/event")
@RestController
public class UserEventController {

	private final UserEventService userEventService;

	@PostMapping("/private/events")
	public ApiResponse<Void> ingest(
		@AuthenticationPrincipal AuthPrincipal auth,
		@RequestBody @Valid UserEventBatchRequest req
	) {
		userEventService.ingest(auth.userId(), req);
		return ApiResponse.ok();
	}
}
