package com.ongi.api.recipe.application;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.recipe.persistence.RecipeAdapter;
import com.ongi.api.recipe.web.dto.CursorPageRequest;
import com.ongi.api.recipe.web.dto.RecipeCardResponse;
import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.enums.PageSortOptionEnum;
import com.ongi.recipe.domain.search.RecipeSearch;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RecipeService {

	private final RecipeAdapter recipeAdapter;

	public ApiResponse<List<RecipeCardResponse>> search(
		CursorPageRequest cursorPageRequest,
		RecipeSearch search
	) {
		RecipeSearchCondition condition = mapToCondition(search);
		Long cursor = cursorPageRequest.cursor();
		int size = cursorPageRequest.resolvedSize();
		PageSortOptionEnum sort = cursorPageRequest.resolveSort();

		// TODO 다른 방식으로도 데이터를 불러올 수 있게 수정
		List<Recipe> recipes = recipeAdapter.search(condition, cursor, size, sort);
		List<RecipeCardResponse> recipeCardResponses =
			recipes.stream()
			.map(this::toRecipeCardResponse)
			.toList();

		return ApiResponse.ok(recipeCardResponses);
	}

	private RecipeSearchCondition mapToCondition(RecipeSearch search) {
		return switch (search) {
			case RecipeSearch.ByKeyword s -> new RecipeSearchCondition(
				s.keyword(), null, null, null, null
			);

			case RecipeSearch.ByTag s -> new RecipeSearchCondition(
				null, s.tag(), null, null, null
			);

			case RecipeSearch.ByCategory s -> new RecipeSearchCondition(
				null, null, s.category(), null, null
			);

			case RecipeSearch.ByIngredient s -> new RecipeSearchCondition(
				null, null, null, s.ingredientId(), null
			);

			case RecipeSearch.ByMaxCookingTimeMin s -> new RecipeSearchCondition(
				null, null, null, null, s.maxCookingTimeMin()
			);

			case RecipeSearch.ByComplex s -> new RecipeSearchCondition(
				s.keyword(), s.tag(), s.category(), s.ingredientId(), s.maxCookingTimeMin()
			);
		};
	}

	private RecipeCardResponse toRecipeCardResponse(Recipe recipe) {
		String cookTimeText = formatCookTime(recipe.getCookingTimeMin());

		Integer servings = null;
		if (recipe.getServing() != null) {
			// 정책에 따라 선택
			servings = (int) Math.round(recipe.getServing()); // 또는 Math.floor / ceil
		}

		String difficultyCode = null;
		if (recipe.getDifficulty() != null) {
			difficultyCode = recipe.getDifficulty().getCode();
		}

		return new RecipeCardResponse(
			recipe.getId(),
			recipe.getTitle(),
			recipe.getImageUrl(),
			recipe.getCookingTimeMin(),
			cookTimeText,
			servings,
			difficultyCode,
			// TODO: rating, likes, comments
			null,
			recipe.getCategory(),
			null,
			null
		);
	}

	private String formatCookTime(Integer minutes) {
		if (minutes == null || minutes <= 0) {
			return null;
		}
		int min = minutes;
		if (min < 60) {
			return min + "분";
		}
		int hours = min / 60;
		int remain = min % 60;
		if (remain == 0) {
			return hours + "시간";
		}
		return hours + "시간 " + remain + "분";
	}

}
