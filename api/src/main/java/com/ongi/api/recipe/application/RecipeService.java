package com.ongi.api.recipe.application;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.ingredients.persistence.IngredientAdapter;
import com.ongi.api.recipe.persistence.RecipeAdapter;
import com.ongi.api.recipe.persistence.RecipeDetailMapper;
import com.ongi.api.recipe.persistence.repository.RecipeLikeRepository;
import com.ongi.api.recipe.persistence.repository.RecipeRepository;
import com.ongi.api.recipe.web.dto.CursorPageRequest;
import com.ongi.api.recipe.web.dto.RecipeCardResponse;
import com.ongi.api.recipe.web.dto.RecipeUpsertRequest;
import com.ongi.api.recipe.web.dto.RecipeDetailBaseResponse;
import com.ongi.api.recipe.web.dto.RecipeIngredientCreateRequest;
import com.ongi.api.recipe.web.dto.RecipeIngredientResponse;
import com.ongi.api.recipe.web.dto.RecipeStepCreateRequest;
import com.ongi.api.recipe.web.dto.RecipeStepsResponse;
import com.ongi.api.recipe.web.dto.RecipeUserFlags;
import com.ongi.ingredients.domain.Ingredient;
import com.ongi.ingredients.domain.RecipeIngredient;
import com.ongi.ingredients.domain.enums.IngredientCategoryEnum;
import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.RecipeSteps;
import com.ongi.recipe.domain.enums.PageSortOptionEnum;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
@Service
public class RecipeService {

	private final RecipeAdapter recipeAdapter;

	private final IngredientAdapter ingredientAdapter;

	private final JPAQueryFactory queryFactory;

	private final RecipeRepository recipeRepository;

	private final RecipeLikeRepository recipeLikeRepository;

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

		List<Recipe> recipes = recipeAdapter.search(condition, cursor, size, sort);
		List<RecipeCardResponse> recipeCardResponses =
			recipes.stream()
				.map(this::toRecipeCardResponse)
				.toList();

		return ApiResponse.ok(recipeCardResponses);
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
			recipe.getLikeCount(),
			recipe.getCommentsCount(),
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

	@Cacheable(
		cacheNames = "recipeDetail",
		key = "#recipeId",
		unless = "#result == null || #result.data == null"
	)
	@Transactional(
		readOnly = true,
		transactionManager = "transactionManager"
	)
	public RecipeDetailBaseResponse getRecipeDetail(Long recipeId) throws NotFoundException {
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

		return new RecipeDetailBaseResponse(
			recipe.getImageUrl(),
			recipe.getTitle(),
			cookTime,
			cookTimeText,
			recipe.getServing() == null ? null : recipe.getServing().intValue(),
			recipe.getDifficulty() != null ? recipe.getDifficulty().getCode() : null,
			recipe.getLikeCount(),
			recipe.getCommentsCount(),
			recipeIngredients,
			recipeSteps);
	}

	@Transactional(readOnly = true, transactionManager = "transactionManager")
	public RecipeUserFlags getFlags(Long recipeId, @Nullable Long userId) {
		if (userId == null) return new RecipeUserFlags(false, false);

		// TODO 좋아요, 저장 개발 시 바꿀 것
		//boolean liked = recipeLikeRepository.existsByUserIdAndRecipeId(userId, recipeId);
		//boolean saved = recipeBookmarkRepository.existsByUserIdAndRecipeId(userId, recipeId);
		//return new RecipeUserFlags(liked, saved);
		return new RecipeUserFlags(false, false);
	}

	@CacheEvict(
		cacheNames = "recipeList",
		allEntries = true
	)
	// TODO 추후 이벤트 발행 시 TransactionManager -> KafkaTM으로 바꿔야할 수도 있음.
	@Transactional(transactionManager = "transactionManager")
	public void createRecipe(RecipeUpsertRequest request) throws NotFoundException {
		Recipe recipe = Recipe.create(
			null,
			request.userId(),
			request.title(),
			request.description(),
			request.serving(),
			request.cookingTimeMin(),
			request.difficulty(),
			request.imageUrl(),
			null,
			null,
			request.category(),
			0L,
			0L
		);

		recipe = recipeAdapter.save(recipe);

		if(!CollectionUtils.isEmpty(request.ingredients())) {
			saveAllRecipeIngredients(recipe.getId(), request.ingredients());
		}

		if(!CollectionUtils.isEmpty(request.steps())) {
			saveAllRecipeSteps(recipe.getId(), request.steps());
		}
	}

	private void saveAllRecipeIngredients(Long recipeId, List<RecipeIngredientCreateRequest> ingredients) throws NotFoundException {
		List<RecipeIngredient> domains = new ArrayList<>();
		for (int i = 0; i < ingredients.size(); i++) {
			RecipeIngredientCreateRequest requestIngredient = ingredients.get(i);

			Ingredient ingredient =
				(requestIngredient.ingredientId() != null )
					? ingredientAdapter.findIngredientById(requestIngredient.ingredientId()).orElseThrow(NotFoundException::new)
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

	private void saveAllRecipeSteps(Long recipeId, List<RecipeStepCreateRequest> steps) throws NotFoundException {
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
	public void updateRecipe(RecipeUpsertRequest request) throws NotFoundException {
		Recipe recipe = recipeAdapter.findRecipeById(request.recipeId()).orElseThrow(NotFoundException::new);

		if(request.userId() != null) {
			recipe.setAuthorId(request.userId());
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

		recipeAdapter.save(recipe);
	}

	@Caching(evict = {
		@CacheEvict(cacheNames = "recipeDetail", key = "#recipeId"),
		@CacheEvict(cacheNames = "recipeList", allEntries = true)
	})
	@Transactional(transactionManager = "transactionManager")
	public void deleteRecipe(Long recipeId) {
		ingredientAdapter.deleteRecipeIngredientByRecipeId(recipeId);
		recipeAdapter.deleteRecipeStepsByRecipeId(recipeId);
		recipeAdapter.deleteRecipeById(recipeId);
	}

	@Transactional(transactionManager = "transactionManager")
	public long like(long recipeId, long userId) {
		boolean inserted = recipeLikeRepository.insertIfNotExists(recipeId, userId);

		if (inserted) {
			recipeRepository.incrementLikeCount(recipeId, 1);
		}

		return recipeRepository.findLikeCount(recipeId);
	}

	@Transactional(transactionManager = "transactionManager")
	public long unlike(long recipeId, long userId) {
		int deleted = recipeLikeRepository.deleteByRecipeIdAndUserId(recipeId, userId);

		if (deleted == 1) {
			recipeRepository.incrementLikeCount(recipeId, -1);
		}

		return recipeRepository.findLikeCount(recipeId);
	}
}
