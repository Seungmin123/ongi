package com.ongi.api.user.web.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record MyPageBasicUpdateRequest(
	@Size(max = 50)
	String name,

	// TODO LocalDate로 변경
	@Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$")
	String birth,

	@Size(max = 10)
	String zipCode,

	@Size(max = 255)
	String address,

	@Size(max = 255)
	String addressDetail
) {

}
