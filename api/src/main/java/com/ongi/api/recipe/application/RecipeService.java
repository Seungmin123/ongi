package com.ongi.api.recipe.application;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.ingredients.persistence.IngredientAdapter;
import com.ongi.api.recipe.persistence.RecipeAdapter;
import com.ongi.api.recipe.persistence.RecipeDetailMapper;
import com.ongi.api.recipe.persistence.repository.RecipeLikeRepository;
import com.ongi.api.recipe.persistence.repository.RecipeStatsRepository;
import com.ongi.api.recipe.web.dto.CommentCreateRequest;
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
import com.ongi.recipe.domain.RecipeComment;
import com.ongi.recipe.domain.RecipeStats;
import com.ongi.recipe.domain.RecipeSteps;
import com.ongi.recipe.domain.enums.PageSortOptionEnum;
import com.ongi.recipe.domain.enums.RecipeCommentStatus;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

	private final RecipeStatsRepository recipeStatsRepository;

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
		unless = "#result == null"
	)
	@Transactional(
		readOnly = true,
		transactionManager = "transactionManager"
	)
	public RecipeDetailBaseResponse getRecipeDetail(Long recipeId) {
		// TODO Ingredient N+1
		Recipe recipe = recipeAdapter.findRecipeById(recipeId).orElseThrow(() -> new IllegalStateException("Recipe not found"));
		List<RecipeIngredientResponse> recipeIngredients =
			ingredientAdapter.findRecipeIngredientByRecipeId(recipeId).stream()
				.map(RecipeDetailMapper::toIngredientResponse)
				.toList();

		List<RecipeStepsResponse> recipeSteps =
			recipeAdapter.findRecipeStepsByRecipeId(recipeId).stream()
				.map(RecipeDetailMapper::toStepsResponse)
				.toList();

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
			recipeIngredients,
			recipeSteps);
	}

	@Transactional(readOnly = true, transactionManager = "transactionManager")
	public RecipeUserFlags getFlags(Long userId, Long recipeId) {
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
	@Transactional(transactionManager = "transactionManager")
	public void createRecipe(Long userId, RecipeUpsertRequest request) {
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

		RecipeStats recipeStats = RecipeStats.create(recipe.getId(), 0L, 0L, 0L);
		recipeAdapter.save(recipeStats);

		if(!CollectionUtils.isEmpty(request.ingredients())) {
			saveAllRecipeIngredients(recipe.getId(), request.ingredients());
		}

		if(!CollectionUtils.isEmpty(request.steps())) {
			saveAllRecipeSteps(recipe.getId(), request.steps());
		}
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
	public void updateRecipe(Long userId, RecipeUpsertRequest request) {
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

		recipeAdapter.save(recipe);
	}

	@Caching(evict = {
		@CacheEvict(cacheNames = "recipeDetail", key = "#recipeId"),
		@CacheEvict(cacheNames = "recipeList", allEntries = true)
	})
	@Transactional(transactionManager = "transactionManager")
	public void deleteRecipe(long userId, long recipeId) {
		Recipe recipe = recipeAdapter.findRecipeById(recipeId).orElseThrow(() -> new IllegalStateException("Recipe not found"));
		if (!recipe.getAuthorId().equals(userId)) {
			throw new SecurityException("forbidden");
		}
		ingredientAdapter.deleteRecipeIngredientByRecipeId(recipeId);
		recipeAdapter.deleteRecipeStepsByRecipeId(recipeId);
		recipeAdapter.deleteRecipeById(recipeId);

		// TODO Likes, Saves, Comments, Tags 모두 삭제 필요.

	}

	@Transactional(transactionManager = "transactionManager")
	public boolean like(long userId, long recipeId) {
		boolean inserted = recipeLikeRepository.insertIfNotExists(userId, recipeId);

		if (inserted) {
			recipeStatsRepository.incrementLikeCount(recipeId, 1);
		}

		return inserted;
	}

	@Transactional(transactionManager = "transactionManager")
	public boolean unlike(long userId, long recipeId) {
		int deleted = recipeLikeRepository.deleteByRecipeIdAndUserId(userId, recipeId);

		if (deleted == 1) {
			recipeStatsRepository.incrementLikeCount(recipeId, -1);
			return true;
		}

		return false;
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public long getRecipeLikeCount(long recipeId) {
		return recipeStatsRepository.findLikeCount(recipeId);
	}

	@Transactional(transactionManager = "transactionManager")
	public Long createRecipeComment(long userId, long recipeId, CommentCreateRequest req) {
		// 1) 레시피 존재 검증
		if (!recipeAdapter.existsRecipeById(recipeId)) {
			throw new IllegalArgumentException("recipe not found: " + recipeId);
		}

		// 2) 엔티티 생성 (대댓글 정책)
		RecipeComment comment;
		if (req.parentId() == null) {
			comment = recipeAdapter.createRootComment(userId, recipeId, req.content());
		} else {
			RecipeComment parent = recipeAdapter
				.findRecipeCommentByIdAndRecipeId(req.parentId(), recipeId)
				.orElseThrow(() -> new IllegalArgumentException("parent not found"));

			if (parent.getStatus() != RecipeCommentStatus.ACTIVE) {
				throw new IllegalStateException("parent deleted");
			}

			long rootId = parent.getRootId();
			int depth = parent.getDepth() + 1;

			comment = recipeAdapter.createReplyComment(userId, recipeId, req.content(), rootId, req.parentId(), depth);
		}

		// 3) 카운트 즉시 반영 (upsert + atomic)
		recipeStatsRepository.upsertIncCommentCount(recipeId, +1);

		return comment.getId();
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

	@Transactional(transactionManager = "transactionManager")
	public boolean deleteRecipeComment(long userId, long recipeId, long commentId) {
		RecipeComment comment = recipeAdapter
			.findRecipeCommentByIdAndRecipeId(commentId, recipeId)
			.orElseThrow(() -> new IllegalArgumentException("comment not found"));

		if (!comment.getUserId().equals(userId)) {
			throw new SecurityException("forbidden");
		}

		boolean deleted = recipeAdapter.deleteRecipeCommentSoft(comment);

		if(deleted) {
			recipeStatsRepository.upsertIncCommentCount(recipeId, -1);
		}

		return deleted;
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public long getRecipeCommentCount(long recipeId) {
		return recipeStatsRepository.findCommentCount(recipeId);
	}
}
