package com.ongi.api.user.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MyPagePersonalizationUpdateRequest(
	@NotNull
	@Size(max = 50)
	List<@Size(max = 50) String> allergens,

	@Min(0)
	@Max(100000) // 정책에 맞게
	Double dietGoal,

	@NotNull
	@Size(max = 200)
	List<@Size(max = 50) String> dislikedIngredients
) {

}
