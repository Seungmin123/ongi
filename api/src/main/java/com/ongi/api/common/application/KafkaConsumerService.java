//package com.ongi.api.common.application;
//
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.stereotype.Service;
//
//@Service
//public class KafkaConsumerService {
//
//	@KafkaListener(topics = "sample-topic", groupId = "sample-group")
//	public void listen(
//		String message,
//		Acknowledgment ack
//	) {
//		try {
//			// 메시지 처리 로직
//			System.out.println("Received: " + message);
//
//			// 정상 처리 후 커밋
//			ack.acknowledge();
//		} catch (Exception e) {
//			// 예외 처리 (재시도, DLQ 등)
//			// ack을 안 하면 재처리 대상이 될 수 있음 (설정에 따라 다름)
//		}
//	}
//}
