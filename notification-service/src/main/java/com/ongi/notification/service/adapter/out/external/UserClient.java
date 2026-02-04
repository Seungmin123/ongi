package com.ongi.notification.service.adapter.out.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserClient {

	private final WebClient.Builder webClientBuilder;

	/**
	 * 유저 서비스로부터 연락처 정보를 가져옴
	 */
	public Mono<UserContactInfo> getUserContact(Long userId) {
		// 실제로는 api 서버의 내 정보 조회 엔드포인트 등을 호출
		// return webClientBuilder.build().get()
		//    .uri("http://api-service/user/internal/{userId}", userId)
		//    ...
		
		// 시뮬레이션: 목 데이터 반환
		return Mono.just(new UserContactInfo("010-1234-5678", "fcm-token-sample"));
	}

	public record UserContactInfo(String phoneNumber, String pushToken) {}
}
