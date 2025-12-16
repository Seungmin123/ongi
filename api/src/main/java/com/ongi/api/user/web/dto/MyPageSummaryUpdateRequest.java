package com.ongi.api.user.web.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record MyPageSummaryUpdateRequest(
	@Size(min = 2, max = 30)
	String displayName,
	UUID profileImageUploadToken,
	String profileImageObjectKey
) {

}
