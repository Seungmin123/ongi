package com.ongi.api.common.messaging.publisher;

import com.ongi.api.common.persistence.enums.OutBoxEventTypeEnum;
import java.util.List;
import tools.jackson.databind.JsonNode;

public class OutboxPublishRouter {
	public static PublishPlan plan(OutBoxEventTypeEnum type, JsonNode payload) {

		return switch (type) {
			case
				RECIPE_CREATED, RECIPE_UPDATED, RECIPE_DELETED
				, RECIPE_COMMENT_CREATED, RECIPE_COMMENT_UPDATED, RECIPE_COMMENT_DELETED
				-> new PublishPlan(List.of(
				new PublishTarget(
					TopicMapper.topicOf(type),
					payload.get("recipeId").asString()
				)
			));

			// Spark Job Topic
			case
				RECIPE_VIEW, RECIPE_LIKED, RECIPE_UNLIKED, RECIPE_BOOKMARKED, RECIPE_UNBOOKMARKED
				-> new PublishPlan(List.of(
				new PublishTarget(
					TopicMapper.topicOf(type),
					payload.get("recipeId").asString()
				),
				new PublishTarget(
					"recipe.engagement.v1",
					payload.get("recipeId").asString()
				)
			));

			case
				COMMUNITY_POST_CREATED, COMMUNITY_POST_UPDATED, COMMUNITY_POST_DELETED, COMMUNITY_POST_VIEW,
				COMMUNITY_COMMENT_CREATED, COMMUNITY_COMMENT_UPDATED, COMMUNITY_COMMENT_DELETED,
				COMMUNITY_POST_LIKED, COMMUNITY_POST_UNLIKED,
				COMMUNITY_COMMENT_LIKED, COMMUNITY_COMMENT_UNLIKED
				-> new PublishPlan(List.of(
				new PublishTarget(
					TopicMapper.topicOf(type),
					payload.get("postId").asString()
				)
			));

		};
	}
}
