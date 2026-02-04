package com.ongi.notification.service.adapter.out.persistence.repository;

import com.ongi.notification.service.adapter.out.persistence.NotificationEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface NotificationRepository extends R2dbcRepository<NotificationEntity, Long> {
	Mono<Boolean> existsByEventId(String eventId);
}
