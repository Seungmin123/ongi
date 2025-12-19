package com.ongi.api.common.messaging.publisher;

import com.ongi.api.common.persistence.enums.OutBoxEventTypeEnum;
import java.util.List;
import tools.jackson.databind.JsonNode;

public class OutboxPublishRouter {
	public static PublishPlan plan(OutBoxEventTypeEnum type, JsonNode payload) {

		return switch (type) {
			case
				RECIPE_CREATED, RECIPE_UPDATED, RECIPE_DELETED, RECIPE_VIEW
				-> new PublishPlan(List.of(
				new PublishTarget(
					TopicMapper.topicOf(type),
					payload.get("recipeId").asString()
				),
				new PublishTarget(
					"user-action-events",
					payload.get("userId").asString()
				)
			));

			case
				RECIPE_LIKED, RECIPE_UNLIKED
				, RECIPE_BOOKMARKED, RECIPE_UNBOOKMARKED
				-> new PublishPlan(List.of(
//				new PublishTarget(
//					"recipe-like-events",
//					payload.get("recipeId").asString()
//				),
				new PublishTarget(
					"user-action-events",
					payload.get("userId").asString()
				)
			));

			case
				RECIPE_COMMENT_CREATED, RECIPE_COMMENT_UPDATED, RECIPE_COMMENT_DELETED
				-> new PublishPlan(List.of(
				new PublishTarget(
					"user-action-events",
					payload.get("userId").asString()
				)
			));

		};
	}
}
