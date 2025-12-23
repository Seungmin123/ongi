package com.ongi.api.community.application.facade;

import com.ongi.api.common.messaging.OutBoxService;
import com.ongi.api.common.persistence.enums.OutBoxAggregateTypeEnum;
import com.ongi.api.common.persistence.enums.OutBoxEventTypeEnum;
import com.ongi.api.community.application.command.CommentCommandService;
import com.ongi.api.community.application.query.CommentQueryService;
import com.ongi.api.community.web.dto.CommentUpsertRequest;
import com.ongi.api.community.web.dto.CommunityPayload;
import com.ongi.api.community.web.dto.LikeResponse;
import com.ongi.api.community.web.dto.PostUpsertRequest;
import com.ongi.api.recipe.web.dto.RecipePayload;
import com.ongi.api.recipe.web.dto.RecipeUpsertRequest;
import com.ongi.recipe.domain.Recipe;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommunityCommentEventFacade {

	private final CommentCommandService commentCommandService;

	private final CommentQueryService commentQueryService;

	private final OutBoxService outBoxService;

	@Transactional(transactionManager = "transactionManager")
	public void createComment(long userId, long postId, CommentUpsertRequest req) throws Exception {
		Long commentId = commentQueryService.create(userId, postId, req.parentId(), req.schema(), req.contentJson());

		if(commentId != null) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.COMMUNITY_COMMENT_CREATED;
			var payload = new CommunityPayload(eventId, userId, postId, commentId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.COMMUNITY, postId, eventType, payload);
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public void updateComment(long userId, long postId, long commentId, CommentUpsertRequest req) throws Exception {
		boolean updated = commentCommandService.update(userId, postId, commentId, req.schema(), req.contentJson());

		if(updated) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.COMMUNITY_COMMENT_UPDATED;
			var payload = new CommunityPayload(eventId, userId, postId, commentId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.COMMUNITY, postId, eventType, payload);
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public void deleteComment(long userId, long postId, long commentId) throws Exception {
		boolean deleted = commentQueryService.delete(userId, postId, commentId);

		if(deleted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.COMMUNITY_COMMENT_DELETED;
			var payload = new CommunityPayload(eventId, userId, postId, commentId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.COMMUNITY, postId, eventType, payload);
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public LikeResponse like(long userId, long postId, long commentId) throws Exception {
		boolean inserted = commentQueryService.like(userId, postId, commentId);

		if(inserted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.COMMUNITY_COMMENT_LIKED;
			var payload = new CommunityPayload(eventId, userId, postId, commentId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.COMMUNITY, postId, eventType, payload);
		}

		long likeCount = commentQueryService.getCommentLikeCount(postId, commentId);
		return new LikeResponse(true, likeCount);
	}

	@Transactional(transactionManager = "transactionManager")
	public LikeResponse unlike(long userId, long postId, long commentId) throws Exception {
		boolean inserted = commentQueryService.unlike(userId, postId, commentId);

		if(inserted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.COMMUNITY_COMMENT_UNLIKED;
			var payload = new CommunityPayload(eventId, userId, postId, commentId, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.COMMUNITY, postId, eventType, payload);
		}

		long likeCount = commentQueryService.getCommentLikeCount(postId, commentId);
		return new LikeResponse(false, likeCount);
	}

}
