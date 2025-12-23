package com.ongi.api.community.application.facade;

import com.ongi.api.common.messaging.OutBoxService;
import com.ongi.api.common.persistence.enums.OutBoxAggregateTypeEnum;
import com.ongi.api.common.persistence.enums.OutBoxEventTypeEnum;
import com.ongi.api.community.adatper.out.cache.PostViewCounter;
import com.ongi.api.community.application.command.PostCommandService;
import com.ongi.api.community.application.query.PostQueryService;
import com.ongi.api.community.web.dto.CommunityPayload;
import com.ongi.api.community.web.dto.LikeResponse;
import com.ongi.api.community.web.dto.PostDetailResponse;
import com.ongi.api.community.web.dto.PostUpsertRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommunityPostEventFacade {

	private final PostCommandService postCommandService;

	private final PostQueryService postQueryService;

	private final OutBoxService outBoxService;

	private final PostViewCounter postViewCounter;

	@Transactional(transactionManager = "transactionManager")
	public void createPost(long userId, PostUpsertRequest req) throws Exception {
		Long postId = postQueryService.create(userId, req.title(), req.schema(), req.contentJson());

		if(postId != null) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.COMMUNITY_POST_CREATED;
			var payload = new CommunityPayload(eventId, userId, postId, null, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.COMMUNITY, postId, eventType, payload);
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public void updatePost(long userId, PostUpsertRequest req) throws Exception {
		Long postId = postCommandService.update(userId, req.postId(), req.title(), req.schema(), req.contentJson());

		if(postId != null) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.COMMUNITY_POST_UPDATED;
			var payload = new CommunityPayload(eventId, userId, postId, null, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.COMMUNITY, postId, eventType, payload);
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public void deletePost(long userId, long postId) throws Exception {
		boolean deleted = postQueryService.delete(userId, postId);

		if(deleted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.COMMUNITY_POST_DELETED;
			var payload = new CommunityPayload(eventId, userId, postId, null, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.COMMUNITY, postId, eventType, payload);
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public PostDetailResponse getPost(Long userId, long postId) throws Exception {
		PostDetailResponse detail = postQueryService.getPost(userId, postId);

		postViewCounter.incr(postId);

		if(userId != null) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.COMMUNITY_POST_VIEW;
			var payload = new CommunityPayload(eventId, userId, postId, null, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.COMMUNITY, postId, eventType, payload);
		}

		return detail;
	}

	@Transactional(transactionManager = "transactionManager")
	public LikeResponse like(long userId, long postId) {
		boolean inserted = postQueryService.like(userId, postId);

		if(inserted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.COMMUNITY_POST_LIKED;
			var payload = new CommunityPayload(eventId, userId, postId, null, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.COMMUNITY, postId, eventType, payload);
		}

		long likeCount = postQueryService.getPostLikeCount(postId);
		return new LikeResponse(true, likeCount);
	}

	@Transactional(transactionManager = "transactionManager")
	public LikeResponse unlike(long userId, long postId) {
		boolean deleted = postQueryService.unlike(userId, postId);

		if(deleted) {
			UUID eventId = UUID.randomUUID();
			OutBoxEventTypeEnum eventType = OutBoxEventTypeEnum.COMMUNITY_POST_UNLIKED;
			var payload = new CommunityPayload(eventId, userId, postId, null, eventType.getCode(), LocalDateTime.now());
			outBoxService.enqueuePending(eventId, OutBoxAggregateTypeEnum.COMMUNITY, postId, eventType, payload);
		}

		long likeCount = postQueryService.getPostLikeCount(postId);
		return new LikeResponse(false, likeCount);
	}

}
