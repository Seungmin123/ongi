package com.ongi.api.recipe.application;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.ingredients.persistence.IngredientAdapter;
import com.ongi.api.recipe.persistence.RecipeAdapter;
import com.ongi.api.recipe.persistence.RecipeDetailMapper;
import com.ongi.api.recipe.web.dto.CursorPageRequest;
import com.ongi.api.recipe.web.dto.RecipeCardResponse;
import com.ongi.api.recipe.web.dto.RecipeDetailResponse;
import com.ongi.api.recipe.web.dto.RecipeIngredientResponse;
import com.ongi.api.recipe.web.dto.RecipeStepsResponse;
import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.enums.PageSortOptionEnum;
import com.ongi.recipe.domain.search.RecipeSearch;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RecipeService {

	private final RecipeAdapter recipeAdapter;

	private final IngredientAdapter ingredientAdapter;

	private final JPAQueryFactory queryFactory;

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
			recipe.getCategory().getName(),
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

	@Transactional(
		readOnly = true,
		transactionManager = "transactionManager"
	)
	public ApiResponse<RecipeDetailResponse> getRecipeDetail(Long recipeId) throws NotFoundException {
		Recipe recipe = recipeAdapter.findRecipeById(recipeId).orElseThrow(NotFoundException::new);
		List<RecipeIngredientResponse> recipeIngredients =
			ingredientAdapter.findRecipeIngredientByRecipeId(recipeId).stream()
				.map(RecipeDetailMapper::toIngredientResponse)
				.toList();

		List<RecipeStepsResponse> recipeSteps =
			recipeAdapter.findRecipeStepsByRecipeId(recipeId).stream()
				.map(RecipeDetailMapper::toStepsResponse)
				.toList();

		Integer cookTime = recipe.getCookingTimeMin();
		String cookTimeText = formatCookTime(cookTime);

		return ApiResponse.ok(
				new RecipeDetailResponse(
					recipe.getImageUrl(),
					recipe.getTitle(),
					cookTime,
					cookTimeText,
					recipe.getServing() == null ? null : recipe.getServing().intValue(),
					recipe.getDifficulty() != null ? recipe.getDifficulty().getCode() : null,
					// TODO recipe.getRating(),
					null,
					recipeIngredients,
					recipeSteps
				));
	}

}
