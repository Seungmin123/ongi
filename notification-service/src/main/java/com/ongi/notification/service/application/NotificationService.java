package com.ongi.notification.service.application;

import com.ongi.notification.domain.NotificationRequest;
import com.ongi.notification.domain.enums.NotificationType;
import com.ongi.notification.service.adapter.out.external.ExternalNotificationClient;
import com.ongi.notification.service.adapter.out.external.UserGrpcClient;
import com.ongi.notification.service.adapter.out.persistence.NotificationEntity;
import com.ongi.notification.service.adapter.out.persistence.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
	public Mono<Void> processNotification(NotificationRequest request) {
		return notificationRepository.existsByEventId(request.eventId())
			.flatMap(exists -> {
				if (exists) {
					log.info("[Skip] 중복 이벤트 감지: {}", request.eventId());
					return Mono.empty();
				}

				// gRPC 호출
				return userClient.getUserContact(request.userId())
					.flatMap(contact -> {
						NotificationEntity notification = NotificationEntity.builder()
							.userId(request.userId())
							.type(request.type())
							.title(request.title())
							.content(request.content())
							.eventId(request.eventId())
							.build();

						return notificationRepository.save(notification)
							.flatMap(saved -> {
								String target = (request.type() == NotificationType.PUSH) 
									? contact.pushToken() : contact.phoneNumber();
								
								return externalNotificationClient.send(saved.getId(), target, saved.getContent())
									.then(notificationRepository.save(saved.markSent()))
									.onErrorResume(e -> notificationRepository.save(saved.markFailed()));
							});
					});
			})
			.then();
	}
}
