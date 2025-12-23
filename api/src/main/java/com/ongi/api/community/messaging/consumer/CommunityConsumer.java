package com.ongi.api.community.messaging.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityConsumer {

	private final CommunityEventHandler communityEventHandler;

	@KafkaListener(
		topics = {
			"community.post.created",
			"community.post.updated",
			"community.post.deleted",
			"community.post.viewed",
			"community.events" // 여기로 POST_LIKED/UNLIKED도 들어오므로 같이 받음
		},
		groupId = "community-api"
	)
	public void onPostMessage(String message, Acknowledgment ack) throws Exception {
		communityEventHandler.postStatusHandle(message);
		ack.acknowledge(); // DB 처리 성공 후 커밋
	}

	@KafkaListener(
		topics = {
			"community.comment.created",
			"community.comment.updated",
			"community.comment.deleted",
			"community.events" // COMMENT_LIKED/UNLIKED도 여기서 처리
		},
		groupId = "community-api"
	)
	public void onCommentMessage(String message, Acknowledgment ack) throws Exception {
		communityEventHandler.commentStatusHandle(message);
		ack.acknowledge(); // DB 처리 성공 후 커밋
	}

}