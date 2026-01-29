package com.ongi.api.user.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MyPagePersonalizationUpdateRequest(
	@NotNull
	@Size(max = 50)
	List<Long> allergenGroupIds,

	@Min(0)
	@Max(100000) // 정책에 맞게
	Double dietGoal,

	@NotNull
	@Size(max = 200)
	List<Long> dislikedIngredientIds
) {

}
