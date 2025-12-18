package com.ongi.api.recipe.application.facade;

import com.ongi.api.common.messaging.OutBoxService;
import com.ongi.api.common.persistence.enums.OutBoxAggregateTypeEnum;
import com.ongi.api.common.persistence.enums.OutBoxEventTypeEnum;
import com.ongi.api.recipe.application.RecipeService;
import com.ongi.api.recipe.application.cache.RecipeViewCounter;
import com.ongi.api.recipe.web.dto.CommentCreateRequest;
import com.ongi.api.recipe.web.dto.CommentCreateResponse;
import com.ongi.api.recipe.web.dto.CommentDeleteResponse;
import com.ongi.api.recipe.web.dto.CommentUpdateRequest;
import com.ongi.api.recipe.web.dto.CommentUpdateResponse;
import com.ongi.api.recipe.web.dto.LikeResponse;
import com.ongi.api.recipe.web.dto.RecipeCommentEventPayload;
import com.ongi.api.recipe.web.dto.RecipeDetailBaseResponse;
import com.ongi.api.recipe.web.dto.RecipeDetailResponse;
import com.ongi.api.recipe.web.dto.RecipeLikePayload;
import com.ongi.api.recipe.web.dto.RecipeUserFlags;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecipeEventFacade {

	private final RecipeService recipeService;

	private final OutBoxService outBoxService;

	private final RecipeViewCounter recipeViewCounter;

	@Transactional(transactionManager = "transactionManager")
	public RecipeDetailResponse view(long recipeId, Long userId) throws Exception {
		RecipeDetailBaseResponse detail = recipeService.getRecipeDetail(recipeId);
		RecipeUserFlags flags = recipeService.getFlags(recipeId, userId);

		// View Incr
		recipeViewCounter.incr(recipeId);

		if(userId != null) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_VIEW;
			var payload = new RecipeLikePayload(eventId, recipeId, userId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		return new RecipeDetailResponse(detail, flags.liked(), flags.saved());
	}

	@Transactional(transactionManager = "transactionManager")
	public LikeResponse like(long recipeId, long userId) {
		boolean inserted = recipeService.like(recipeId, userId);

		if(inserted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_LIKED;
			var payload = new RecipeLikePayload(eventId, recipeId, userId, eventType.getCode(), LocalDateTime.now());

			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		long likeCount = recipeService.getRecipeLikeCount(recipeId);
		return new LikeResponse(true, likeCount);
	}

	@Transactional(transactionManager = "transactionManager")
	public LikeResponse unlike(long recipeId, long userId) {
		boolean deleted = recipeService.unlike(recipeId, userId);

		if (deleted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_UNLIKED;
			var payload = new RecipeLikePayload(eventId, recipeId, userId, eventType.getCode(), LocalDateTime.now());

			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		long likeCount = recipeService.getRecipeLikeCount(recipeId);
		return new LikeResponse(false, likeCount);
	}

	@Transactional(transactionManager = "transactionManager")
	public CommentCreateResponse createRecipeComment(long recipeId, long userId, CommentCreateRequest req) {
		Long commentId = recipeService.createRecipeComment(recipeId, userId, req);

		if(commentId != null) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_COMMENT_CREATED;
			var payload = new RecipeCommentEventPayload(eventId, recipeId, commentId, userId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		Long commentCount = recipeService.getRecipeCommentCount(recipeId);
		return new CommentCreateResponse(commentId, recipeId, commentCount);
	}

	@Transactional(transactionManager = "transactionManager")
	public CommentUpdateResponse updateRecipeComment(long recipeId, long commentId, long userId, CommentUpdateRequest req) {
		boolean updated = recipeService.updateRecipeComment(recipeId, commentId, userId, req.content());

		if(updated) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_COMMENT_UPDATED;
			var payload = new RecipeCommentEventPayload(eventId, recipeId, commentId, userId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		return new CommentUpdateResponse(commentId, recipeId);
	}

	@Transactional(transactionManager = "transactionManager")
	public CommentDeleteResponse deleteRecipeComment(long recipeId, long commentId, long userId) {
		boolean deleted = recipeService.deleteRecipeComment(recipeId, commentId, userId);

		if (deleted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.RECIPE_COMMENT_DELETED;
			var payload = new RecipeCommentEventPayload(eventId, recipeId, commentId, userId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.RECIPE, recipeId, eventType, payload);
		}

		Long commentCount = recipeService.getRecipeCommentCount(recipeId);
		return new CommentDeleteResponse(commentId, recipeId, commentCount);
	}
}
