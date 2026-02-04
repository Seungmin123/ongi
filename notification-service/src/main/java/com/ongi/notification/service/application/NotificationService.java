package com.ongi.notification.service.application;

import com.ongi.notification.domain.NotificationRequest;
import com.ongi.notification.domain.enums.NotificationType;
import com.ongi.notification.service.adapter.out.external.ExternalNotificationClient;
import com.ongi.notification.service.adapter.out.external.UserGrpcClient;
import com.ongi.notification.service.adapter.out.persistence.NotificationEntity;
import com.ongi.notification.service.adapter.out.persistence.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final ExternalNotificationClient externalNotificationClient;
	private final UserGrpcClient userClient;

	/**
	 * 통합 알림 처리 로직 (gRPC를 통한 사용자 정보 조회 포함)
	 */
	@Transactional
	public Mono<Void> processNotification(NotificationRequest request) {
		// 1. 사용자 정보 조회 (gRPC)
		return userClient.getUserContact(request.userId())
			.flatMap(contact -> {
				NotificationEntity notification = NotificationEntity.builder()
					.userId(request.userId())
					.type(request.type())
					.title(request.title())
					.content(request.content())
					.eventId(request.eventId())
					.build();

				// 2. 초기 저장 (PENDING) - event_id 유니크 제약조건에 걸리면 무시
				return notificationRepository.save(notification)
					.flatMap(saved -> {
						String target = (request.type() == NotificationType.PUSH)
							? contact.pushToken() : contact.phoneNumber();

						// 3. 외부 발송 및 상태 업데이트
						return externalNotificationClient.send(saved.getId(), target, saved.getContent())
							.then(notificationRepository.save(saved.markSent()))
							.onErrorResume(e -> {
								log.error("알림 발송 실패: {}", e.getMessage());
								return notificationRepository.save(saved.markFailed());
							});
					});
			})
			// 4. 중복 이벤트(DataIntegrityViolationException) 발생 시 조용히 무시 (멱등성 보장)
			.onErrorResume(DataIntegrityViolationException.class, e -> {
				log.info("[Skip] 중복 이벤트 감지 (DB Constraint): {}", request.eventId());
				return Mono.empty();
			})
			.then();
	}
}
