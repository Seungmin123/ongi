package com.ongi.api.recipe.application.command;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.ingredients.persistence.IngredientAdapter;
import com.ongi.api.recipe.adapter.out.cache.RecipeCacheReader;
import com.ongi.api.recipe.adapter.out.persistence.RecipeAdapter;
import com.ongi.api.recipe.adapter.out.persistence.RecipeDetailMapper;
import com.ongi.api.recipe.messaging.consumer.RecipeCacheVersionResolver;
import com.ongi.api.recipe.web.dto.CursorPageRequest;
import com.ongi.api.recipe.web.dto.RecipeCardResponse;
import com.ongi.api.recipe.web.dto.RecipeUpsertRequest;
import com.ongi.api.recipe.web.dto.RecipeDetailBaseResponse;
import com.ongi.api.recipe.web.dto.RecipeIngredientCreateRequest;
import com.ongi.api.recipe.web.dto.RecipeIngredientResponse;
import com.ongi.api.recipe.web.dto.RecipeStepCreateRequest;
import com.ongi.api.recipe.web.dto.RecipeStepsResponse;
import com.ongi.recipe.domain.RecipeUserFlags;
import com.ongi.ingredients.domain.Ingredient;
import com.ongi.ingredients.domain.RecipeIngredient;
import com.ongi.ingredients.domain.enums.IngredientCategoryEnum;
import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.RecipeComment;
import com.ongi.recipe.domain.RecipeStats;
import com.ongi.recipe.domain.RecipeSteps;
import com.ongi.recipe.domain.enums.PageSortOptionEnum;
import com.ongi.recipe.domain.enums.RecipeCommentStatus;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
@Service
public class RecipeService {

	private final RecipeCacheReader recipeCacheReader;

	private final RecipeCacheVersionResolver recipeCacheVersionResolver;

	private final RecipeAdapter recipeAdapter;

	private final IngredientAdapter ingredientAdapter;

	@Cacheable(
		value = "recipeList",
		key = "T(java.lang.String).format('%s:%s:%s:%s', " +
			"#cursorPageRequest.cursor(), " +
			"#cursorPageRequest.resolvedSize(), " +
			"#cursorPageRequest.resolveSort(), " +
			"#condition.toKeyString()" +
			")",
		condition = "#cursorPageRequest.cursor() == null",
		unless = "#result == null || #result.data == null || #result.data.isEmpty()"
	)
	public ApiResponse<List<RecipeCardResponse>> search(
		CursorPageRequest cursorPageRequest,
		RecipeSearchCondition condition
	) {
		Long cursor = cursorPageRequest.cursor();
		int size = cursorPageRequest.resolvedSize();
		PageSortOptionEnum sort = cursorPageRequest.resolveSort();

		// TODO RecipeStats 추가함 해결 필요.
		List<Recipe> recipes = recipeAdapter.search(condition, cursor, size, sort);
		if (recipes.isEmpty()) {
			return ApiResponse.ok(List.of());
		}

		List<Long> recipeIds = recipes.stream().map(Recipe::getId).toList();

		Map<Long, RecipeStats> statsMap =
			recipeAdapter.findRecipeStatsByRecipeIds(recipeIds).stream()
				.collect(java.util.stream.Collectors.toMap(RecipeStats::getRecipeId, s -> s));

		List<RecipeCardResponse> recipeCardResponses = recipes.stream()
			.map(r -> toRecipeCardResponse(r, statsMap.get(r.getId())))
			.toList();

		return ApiResponse.ok(recipeCardResponses);
	}

	private RecipeCardResponse toRecipeCardResponse(Recipe recipe, RecipeStats stats) {
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
			stats.getLikeCount(),
			stats.getCommentCount(),
			stats.getBookmarkCount(),
			recipe.getCategory().getName()
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

	@Transactional(readOnly = true, transactionManager = "transactionManager")
	public RecipeDetailBaseResponse getRecipeDetail(Long recipeId) {
		int ver = recipeCacheVersionResolver.getOrInit(recipeId);
		Recipe recipe = recipeCacheReader.getRecipeById(recipeId, ver);
		List<RecipeIngredientResponse> recipeIngredients = recipeCacheReader.getRecipeIngredients(recipeId, ver);
		List<RecipeStepsResponse> recipeSteps = recipeCacheReader.getRecipeSteps(recipeId, ver);

		RecipeStats recipeStats = recipeAdapter.findRecipeStatsByRecipeId(recipeId).orElseThrow(() -> new IllegalStateException("Recipe Stats not found"));

		Integer cookTime = recipe.getCookingTimeMin();
		String cookTimeText = formatCookTime(cookTime);

		return new RecipeDetailBaseResponse(
			recipe.getImageUrl(),
			recipe.getTitle(),
			cookTime,
			cookTimeText,
			recipe.getServing() == null ? null : recipe.getServing().intValue(),
			recipe.getDifficulty() != null ? recipe.getDifficulty().getCode() : null,
			recipeStats.getLikeCount(),
			recipeStats.getCommentCount(),
			recipeStats.getBookmarkCount(),
			recipeIngredients,
			recipeSteps);
	}

	@Transactional(readOnly = true, transactionManager = "transactionManager")
	public RecipeUserFlags getFlags(Long userId, Long recipeId) {
		if (userId == null) return new RecipeUserFlags(false, false);
		return recipeAdapter.getFlags(userId, recipeId);
	}

	@CacheEvict(
		cacheNames = "recipeList",
		allEntries = true
	)
	@Transactional(transactionManager = "transactionManager")
	public Recipe createRecipe(Long userId, RecipeUpsertRequest request) {
		Recipe recipe = Recipe.create(
			null,
			userId,
			request.title(),
			request.description(),
			request.serving(),
			request.cookingTimeMin(),
			request.difficulty(),
			request.imageUrl(),
			null,
			null,
			request.category()
		);

		recipe = recipeAdapter.save(recipe);

		RecipeStats recipeStats = RecipeStats.create(recipe.getId(), 0L, 0L, 0L, 0L);
		recipeAdapter.save(recipeStats);

		if(!CollectionUtils.isEmpty(request.ingredients())) {
			saveAllRecipeIngredients(recipe.getId(), request.ingredients());
		}

		if(!CollectionUtils.isEmpty(request.steps())) {
			saveAllRecipeSteps(recipe.getId(), request.steps());
		}

		return recipe;
	}

	private void saveAllRecipeIngredients(Long recipeId, List<RecipeIngredientCreateRequest> ingredients) {
		List<RecipeIngredient> domains = new ArrayList<>();
		for (int i = 0; i < ingredients.size(); i++) {
			RecipeIngredientCreateRequest requestIngredient = ingredients.get(i);

			Ingredient ingredient =
				(requestIngredient.ingredientId() != null )
					? ingredientAdapter.findIngredientById(requestIngredient.ingredientId()).orElseThrow(() -> new IllegalStateException("Ingredient not found"))
					: ingredientAdapter.findLikeOrCreateIngredient(requestIngredient.name(), IngredientCategoryEnum.UNKNOWN, 0.0, 0.0, 0.0, 0.0);

			RecipeIngredient domain = RecipeIngredient.create(
				null,
				recipeId,
				ingredient,
				requestIngredient.quantity(),
				requestIngredient.unit(),
				requestIngredient.note(),
				i
			);

			domains.add(domain);
		}
		ingredientAdapter.saveAllRecipeIngredients(domains);
	}

	private void saveAllRecipeSteps(Long recipeId, List<RecipeStepCreateRequest> steps) {
		List<RecipeSteps> domains = new ArrayList<>();
		for(int i = 0; i < steps.size(); i++) {
			RecipeStepCreateRequest domain = steps.get(i);
			domains.add(
				RecipeSteps.create(
					null, recipeId, i + 1, "STEP " + (i + 1), domain.description()
					, 0, null, null
					,domain.imageUrl(), null)
			);
		}
		recipeAdapter.saveAllRecipeSteps(domains);
	}

	@Caching(evict = {
		@CacheEvict(cacheNames = "recipeDetail", key = "#request.recipeId()"),
		@CacheEvict(cacheNames = "recipeList", allEntries = true)
	})
	@Transactional(transactionManager = "transactionManager")
	public Recipe updateRecipe(Long userId, RecipeUpsertRequest request) {
		Recipe recipe = recipeAdapter.findRecipeById(request.recipeId()).orElseThrow(() -> new IllegalStateException("Recipe not found"));

		if (!recipe.getAuthorId().equals(userId)) {
			throw new SecurityException("forbidden");
		}

		if(request.title() != null) {
			recipe.setTitle(request.title());
		}

		if(request.description() != null) {
			recipe.setDescription(request.description());
		}

		if(request.category() != null) {
			recipe.setCategory(request.category());
		}

		if(request.difficulty() != null) {
			recipe.setDifficulty(request.difficulty());
		}

		if(request.cookingTimeMin() != null) {
			recipe.setCookingTimeMin(request.cookingTimeMin());
		}

		if(request.serving() != null) {
			recipe.setServing(request.serving());
		}

		if(request.imageUrl() != null) {
			recipe.setImageUrl(request.imageUrl());
		}

		if(request.ingredients() != null) {
			ingredientAdapter.deleteRecipeIngredientByRecipeId(request.recipeId());
			saveAllRecipeIngredients(recipe.getId(), request.ingredients());
		}

		if(request.steps() != null) {
			recipeAdapter.deleteRecipeStepsByRecipeId(request.recipeId());
			saveAllRecipeSteps(recipe.getId(), request.steps());
		}

		return recipeAdapter.save(recipe);
	}

	@Caching(evict = {
		@CacheEvict(cacheNames = "recipeDetail", key = "#recipeId"),
		@CacheEvict(cacheNames = "recipeList", allEntries = true)
	})
	@Transactional(transactionManager = "transactionManager")
	public boolean deleteRecipe(long userId, long recipeId) {
		Recipe recipe = recipeAdapter.findRecipeById(recipeId).orElseThrow(() -> new IllegalStateException("Recipe not found"));
		if (!recipe.getAuthorId().equals(userId)) {
			throw new SecurityException("forbidden");
		}
		ingredientAdapter.deleteRecipeIngredientByRecipeId(recipeId);
		recipeAdapter.deleteRecipeStepsByRecipeId(recipeId);
		recipeAdapter.deleteRecipeById(recipeId);

		// TODO Likes, Saves, Comments, Tags 모두 삭제 필요.

		return true;

	}

	@Transactional(transactionManager = "transactionManager")
	public boolean updateRecipeComment(long userId, long recipeId, long commentId, String content) {
		RecipeComment comment = recipeAdapter
			.findRecipeCommentByIdAndRecipeIdAndStatus(commentId, recipeId, RecipeCommentStatus.ACTIVE)
			.orElseThrow(() -> new IllegalArgumentException("comment not found"));

		if (!comment.getUserId().equals(userId)) {
			throw new SecurityException("forbidden");
		}

		recipeAdapter.updateRecipeCommentContent(comment, content);

		return true;
	}

}
