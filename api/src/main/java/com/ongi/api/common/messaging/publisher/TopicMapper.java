package com.ongi.api.common.messaging.publisher;

import com.ongi.api.common.persistence.enums.OutBoxEventTypeEnum;

public final class TopicMapper {
	private TopicMapper() {}

	public static String topicOf(OutBoxEventTypeEnum type) {
		return switch (type) {
			case RECIPE_CREATED -> "recipe.created";
			case RECIPE_UPDATED -> "recipe.updated";
	        case RECIPE_DELETED -> "recipe.deleted";
		    case RECIPE_VIEW -> "recipe.viewed";
		    case RECIPE_LIKED, RECIPE_UNLIKED,
			     RECIPE_BOOKMARKED, RECIPE_UNBOOKMARKED,
			     RECIPE_COMMENT_CREATED, RECIPE_COMMENT_UPDATED, RECIPE_COMMENT_DELETED
				-> "recipe.events"; // 도메인 단일 토픽(추천)
			case COMMUNITY_POST_CREATED -> "community.post.created";
			case COMMUNITY_POST_UPDATED -> "community.post.updated";
			case COMMUNITY_POST_DELETED -> "community.post.deleted";
			case COMMUNITY_POST_VIEW -> "community.post.viewed";
			case COMMUNITY_COMMENT_CREATED -> "community.comment.created";
			case COMMUNITY_COMMENT_UPDATED -> "community.comment.updated";
			case COMMUNITY_COMMENT_DELETED -> "community.comment.deleted";
			case COMMUNITY_POST_LIKED, COMMUNITY_POST_UNLIKED,
			     COMMUNITY_COMMENT_LIKED, COMMUNITY_COMMENT_UNLIKED
				-> "community.events";
		};
	}
}
