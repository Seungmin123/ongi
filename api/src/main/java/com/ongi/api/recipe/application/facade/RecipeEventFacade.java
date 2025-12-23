package com.ongi.api.recipe.application.facade;

import com.ongi.api.common.messaging.OutBoxService;
import com.ongi.api.common.persistence.enums.OutBoxAggregateTypeEnum;
import com.ongi.api.common.persistence.enums.OutBoxEventTypeEnum;
import com.ongi.api.recipe.application.command.RecipeService;
import com.ongi.api.recipe.adapter.out.cache.RecipeViewCounter;
import com.ongi.api.recipe.application.query.RecipeQueryService;
import com.ongi.api.recipe.web.dto.BookmarkResponse;
import com.ongi.api.recipe.web.dto.CommentCreateRequest;
import com.ongi.api.recipe.web.dto.CommentCreateResponse;
import com.ongi.api.recipe.web.dto.CommentDeleteResponse;
import com.ongi.api.recipe.web.dto.CommentUpdateRequest;
import com.ongi.api.recipe.web.dto.CommentUpdateResponse;
import com.ongi.api.recipe.web.dto.LikeResponse;
import com.ongi.api.recipe.web.dto.RecipeBookmarkPayload;
import com.ongi.api.recipe.web.dto.RecipeCommentEventPayload;
import com.ongi.api.recipe.web.dto.RecipeDetailBaseResponse;
import com.ongi.api.recipe.web.dto.RecipeDetailResponse;
import com.ongi.api.recipe.web.dto.RecipeLikePayload;
import com.ongi.api.recipe.web.dto.RecipePayload;
import com.ongi.api.recipe.web.dto.RecipeUpsertRequest;
import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.RecipeUserFlags;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RecipeEventFacade {

	private final RecipeService recipeService;

	private final RecipeQueryService recipeQueryService;

	private final OutBoxService outBoxService;

	private final RecipeViewCounter recipeViewCounter;

	@Transactional(transactionManager = "transactionManager")
	public void createRecipe(long userId, RecipeUpsertRequest recipeUpsertRequest) throws Exception {
		Recipe recipe = recipeService.createRecipe(userId, recipeUpsertRequest);

		if(recipe != null) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_CREATED;
			var payload = new RecipePayload(eventId, userId, recipe.getId(), eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipe.getId(), eventType, payload);
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public void updateRecipe(long userId, RecipeUpsertRequest recipeUpsertRequest) throws Exception {
		Recipe recipe = recipeService.updateRecipe(userId, recipeUpsertRequest);

		if(recipe != null) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_UPDATED;
			var payload = new RecipePayload(eventId, userId, recipe.getId(), eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipe.getId(), eventType, payload);
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public void deleteRecipe(long userId, long recipeId) throws Exception {
		boolean deleted = recipeService.deleteRecipe(userId, recipeId);

		if(deleted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_DELETED;
			var payload = new RecipePayload(eventId, userId, recipeId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public RecipeDetailResponse view(Long userId, long recipeId) throws Exception {
		RecipeDetailBaseResponse detail = recipeService.getRecipeDetail(recipeId);
		RecipeUserFlags flags = recipeService.getFlags(userId, recipeId);

		// View Incr
		recipeViewCounter.incr(recipeId);

		if(userId != null) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_VIEW;
			var payload = new RecipeLikePayload(eventId, userId, recipeId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		return new RecipeDetailResponse(detail, flags.liked(), flags.saved());
	}

	@Transactional(transactionManager = "transactionManager")
	public LikeResponse like(long userId, long recipeId) {
		boolean inserted = recipeQueryService.like(userId, recipeId);

		if(inserted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_LIKED;
			var payload = new RecipeLikePayload(eventId, userId, recipeId, eventType.getCode(), LocalDateTime.now());

			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		long likeCount = recipeQueryService.getRecipeLikeCount(recipeId);
		return new LikeResponse(true, likeCount);
	}

	@Transactional(transactionManager = "transactionManager")
	public LikeResponse unlike(long userId, long recipeId) {
		boolean deleted = recipeQueryService.unlike(userId, recipeId);

		if (deleted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_UNLIKED;
			var payload = new RecipeLikePayload(eventId, userId, recipeId, eventType.getCode(), LocalDateTime.now());

			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		long likeCount = recipeQueryService.getRecipeLikeCount(recipeId);
		return new LikeResponse(false, likeCount);
	}

	@Transactional(transactionManager = "transactionManager")
	public BookmarkResponse bookmark(long userId, long recipeId) {
		boolean inserted = recipeQueryService.bookmark(userId, recipeId);

		if(inserted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_BOOKMARKED;
			var payload = new RecipeBookmarkPayload(eventId, userId, recipeId, eventType.getCode(), LocalDateTime.now());

			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		long bookmarkCount = recipeQueryService.getRecipeBookmarkCount(recipeId);
		return new BookmarkResponse(true, bookmarkCount);
	}

	@Transactional(transactionManager = "transactionManager")
	public BookmarkResponse unbookmark(long userId, long recipeId) {
		boolean deleted = recipeQueryService.unbookmark(userId, recipeId);

		if (deleted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_UNBOOKMARKED;
			var payload = new RecipeLikePayload(eventId, userId, recipeId, eventType.getCode(), LocalDateTime.now());

			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		long bookmarkCount = recipeQueryService.getRecipeBookmarkCount(recipeId);
		return new BookmarkResponse(false, bookmarkCount);
	}

	@Transactional(transactionManager = "transactionManager")
	public CommentCreateResponse createRecipeComment(long userId, long recipeId, CommentCreateRequest req) {
		Long commentId = recipeQueryService.createRecipeComment(userId, recipeId, req);

		if(commentId != null) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_COMMENT_CREATED;
			var payload = new RecipeCommentEventPayload(eventId, userId, recipeId, commentId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		Long commentCount = recipeQueryService.getRecipeCommentCount(recipeId);
		return new CommentCreateResponse(commentId, recipeId, commentCount);
	}

	@Transactional(transactionManager = "transactionManager")
	public CommentUpdateResponse updateRecipeComment(long userId, long recipeId, long commentId, CommentUpdateRequest req) {
		boolean updated = recipeService.updateRecipeComment(userId, recipeId, commentId, req.content());

		if(updated) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_COMMENT_UPDATED;
			var payload = new RecipeCommentEventPayload(eventId, userId, recipeId, commentId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		return new CommentUpdateResponse(commentId, recipeId);
	}

	@Transactional(transactionManager = "transactionManager")
	public CommentDeleteResponse deleteRecipeComment(long userId, long recipeId, long commentId) {
		boolean deleted = recipeQueryService.deleteRecipeComment(userId, recipeId, commentId);

		if (deleted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_COMMENT_DELETED;
			var payload = new RecipeCommentEventPayload(eventId, userId, recipeId, commentId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		Long commentCount = recipeQueryService.getRecipeCommentCount(recipeId);
		return new CommentDeleteResponse(commentId, recipeId, commentCount);
	}
}
