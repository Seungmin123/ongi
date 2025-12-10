package com.ongi.api.common.application;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public void sendMessage(String topic, String key, String message) {
		kafkaTemplate.send(topic, key, message)
			.whenComplete((result, ex) -> {
				if (ex != null) {
					// 로깅 + 알람 포인트
					System.err.println("Failed to send message: " + ex.getMessage());
				} else {
					// 성공 로그 (필요시)
					// System.out.println("Sent to partition " + result.getRecordMetadata().partition());
				}
			});
	}
}
