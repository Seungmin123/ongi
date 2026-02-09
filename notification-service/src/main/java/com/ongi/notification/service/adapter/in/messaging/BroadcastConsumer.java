package com.ongi.notification.service.adapter.in.messaging;

import tools.jackson.databind.ObjectMapper;
import com.ongi.notification.domain.BroadcastRequest;
import com.ongi.notification.domain.NotificationRequest;
import com.ongi.notification.service.adapter.out.external.UserGrpcClient;
import com.ongi.notification.service.application.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BroadcastConsumer {

	private final KafkaReceiver<String, String> broadcastKafkaReceiver;
	private final NotificationService notificationService;
	private final UserGrpcClient userGrpcClient;
	private final ObjectMapper objectMapper;

	@EventListener(ApplicationReadyEvent.class)
	public void startConsuming() {
		broadcastKafkaReceiver.receive()
			.flatMap(record -> {
				log.info("[Broadcast] 대량 알림 요청 수신 - Topic: {}, Offset: {}", record.topic(), record.offset());
				
				return processBroadcast(record)
					.retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
						.doAfterRetry(signal -> log.warn("브로드캐스트 처리 재시도 중... {}", signal.failure().getMessage())))
					.onErrorResume(e -> {
						log.error("브로드캐스트 처리 최종 실패: {}", record.value(), e);
						return Mono.empty();
					})
					.then(record.receiverOffset().commit());
			})
			.subscribe(
				null,
				e -> log.error("Broadcast Consumer 스트림 비정상 종료", e)
			);
	}

	private Mono<Void> processBroadcast(ReceiverRecord<String, String> record) {
		return Mono.fromCallable(() -> objectMapper.readValue(record.value(), BroadcastRequest.class))
			.flatMap(request -> {
				// 브로드캐스트용 고유 ID 생성 (멱등성 보장용)
				String broadcastId = "BROADCAST_" + UUID.randomUUID().toString().substring(0, 8);
				log.info("[Broadcast] 작업 시작 - ID: {}, Target: {}", broadcastId, request.targetGroup());

				// 1. 유저 스트림 수신 (Fan-out)
				return userGrpcClient.streamUsers(request.targetGroup())
					// 2. NotificationRequest로 변환
					.map(user -> new NotificationRequest(
						user.userId(),
						request.type(),
						request.title(),
						request.content(),
						broadcastId + ":" + user.userId(), // EventID = BroadcastID + UserID
						null
					))
					// 3. 500개씩 묶어서 배치 처리
					.buffer(500)
					.flatMap(batch -> {
						log.debug("[Broadcast] {}건 배치 발송 진행 중...", batch.size());
						return notificationService.processNotifications(batch);
					}) // concurrency 기본값(256)으로 병렬 실행됨
					.then()
					.doOnSuccess(v -> log.info("[Broadcast] 작업 완료 - ID: {}", broadcastId));
			});
	}
}
