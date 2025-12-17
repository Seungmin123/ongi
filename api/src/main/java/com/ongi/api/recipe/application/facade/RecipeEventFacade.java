package com.ongi.api.recipe.application.facade;

import com.ongi.api.common.application.OutBoxService;
import com.ongi.api.common.persistence.enums.OutBoxAggregateTypeEnum;
import com.ongi.api.common.persistence.enums.OutBoxEventTypeEnum;
import com.ongi.api.recipe.application.RecipeService;
import com.ongi.api.recipe.web.dto.LikeResponse;
import com.ongi.api.recipe.web.dto.RecipeLikePayload;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class RecipeEventFacade {

	private final RecipeService recipeService;

	private final OutBoxService outBoxService;

	private final ObjectMapper objectMapper;

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
}
