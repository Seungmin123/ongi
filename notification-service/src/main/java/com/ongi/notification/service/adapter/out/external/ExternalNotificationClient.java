package com.ongi.notification.service.adapter.out.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Slf4j
@Component
public class ExternalNotificationClient {

	/**
	 * 실제 카카오톡이나 FCM API를 호출하는 부분
	 */
	public Mono<Void> send(Long notificationId, String target, String content) {
		return Mono.delay(Duration.ofMillis(500))
			.doOnNext(it -> log.info("[External] 알림 발송 완료 - ID: {}, Target: {}", notificationId, target))
			.then();
	}
}
