package com.ongi.notification.service.application;

import com.ongi.notification.domain.NotificationRequest;
import com.ongi.notification.domain.enums.NotificationType;
import com.ongi.notification.service.adapter.out.external.ExternalNotificationClient;
import com.ongi.notification.service.adapter.out.external.UserGrpcClient;
import com.ongi.notification.service.adapter.out.external.UserGrpcClient.UserContactInfo;
import com.ongi.notification.service.adapter.out.persistence.NotificationEntity;
import com.ongi.notification.service.adapter.out.persistence.repository.NotificationRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final ExternalNotificationClient externalNotificationClient;
	private final UserGrpcClient userClient;

	/**
	 * [단건 처리] 실시간성이 매우 중요할 때 사용
	 */
	@Transactional
	public Mono<Void> processNotification(NotificationRequest request) {
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
					.flatMap(saved -> sendAndUpdateStatus(saved, contact)
						.flatMap(notificationRepository::save));
			})
			.onErrorResume(DataIntegrityViolationException.class, e -> {
				log.info("[Skip] 중복 이벤트 감지 (DB Constraint): {}", request.eventId());
				return Mono.empty();
			})
			.then();
	}

	/**
	 * [배치 처리] Consumer의 bufferTimeout과 연동하여 대량 처리에 최적화
	 * Flow: 병렬 유저조회 -> Bulk Insert -> 병렬 발송 -> Bulk Update
	 */
	@Transactional
	public Mono<Void> processNotifications(List<NotificationRequest> requests) {
		if (requests.isEmpty()) return Mono.empty();

		// 1. gRPC 병렬 조회 및 (Request, Contact) 페어링
		return Flux.fromIterable(requests)
			.flatMap(req -> userClient.getUserContact(req.userId())
				.map(contact -> Tuples.of(req, contact))
				.onErrorResume(e -> {
					log.error("유저 정보 조회 실패 (Skip): userId={}, error={}", req.userId(), e.getMessage());
					return Mono.empty();
				}))
			.collectList()
			.flatMap(validPairs -> {
				// 나중에 발송할 때 사용하기 위해 Map으로 연락처 정보 보관
				Map<String, UserContactInfo> contactMap = validPairs.stream()
					.collect(Collectors.toMap(
						p -> p.getT1().eventId(), // Key: EventID
						p -> p.getT2()            // Value: ContactInfo
					));

				// 엔티티 변환
				List<NotificationEntity> newEntities = validPairs.stream()
					.map(p -> NotificationEntity.builder()
						.userId(p.getT1().userId())
						.type(p.getT1().type())
						.title(p.getT1().title())
						.content(p.getT1().content())
						.eventId(p.getT1().eventId())
						.build())
					.toList();

				// 2. Bulk Insert (saveAll)
				return notificationRepository.saveAll(newEntities)
					.flatMap(saved -> {
						// 3. 병렬 발송
						UserContactInfo contact = contactMap.get(saved.getEventId());
						return sendAndUpdateStatus(saved, contact);
					})
					.collectList()
					.flatMap(updatedEntities -> 
						// 4. Bulk Update (상태 변경 반영)
						notificationRepository.saveAll(updatedEntities).then()
					);
			})
			.onErrorResume(DataIntegrityViolationException.class, e -> {
				log.warn("배치 처리 중 중복 데이터 발생으로 전체 롤백됨: {}", e.getMessage());
				return Mono.empty();
			});
	}

	// 공통 로직: 발송 및 상태 업데이트된 엔티티 반환 (저장은 하지 않음)
	private Mono<NotificationEntity> sendAndUpdateStatus(NotificationEntity entity, UserContactInfo contact) {
		String target = (entity.getType() == NotificationType.PUSH)
			? contact.pushToken() : contact.phoneNumber();

		return externalNotificationClient.send(entity.getId(), target, entity.getContent())
			.map(v -> entity.markSent()) // 성공 시 SENT 상태로 변경
			.onErrorResume(e -> {
				log.error("발송 실패 - ID: {}, Error: {}", entity.getId(), e.getMessage());
				return Mono.just(entity.markFailed()); // 실패 시 FAILED 상태로 변경
			});
	}
}