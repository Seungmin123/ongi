package com.ongi.api.order.adapter.out.payment.toss;

import com.ongi.api.config.properties.TossPaymentProperties;
import com.ongi.api.order.application.port.PaymentClient;
import java.util.Base64;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class TossPaymentClient implements PaymentClient {

	private final WebClient webClient;
	private final TossPaymentProperties properties;

	public TossPaymentClient(WebClient.Builder webClientBuilder, TossPaymentProperties properties) {
		this.properties = properties;
		this.webClient = webClientBuilder.baseUrl(properties.baseUrl()).build();
	}

	@Override
	public TossPaymentConfirmResponse confirm(String paymentKey, String orderId, Long amount) {
		log.info("Toss Payment 승인 요청: orderId={}, amount={}", orderId, amount);

		String encodedSecretKey = Base64.getEncoder().encodeToString((properties.secretKey() + ":").getBytes());

		return webClient.post()
			.uri("/v1/payments/confirm")
			.header("Authorization", "Basic " + encodedSecretKey)
			.bodyValue(Map.of(
				"paymentKey", paymentKey,
				"orderId", orderId,
				"amount", amount
			))
			.retrieve()
			.onStatus(HttpStatusCode::isError, response -> {
				return response.bodyToMono(String.class)
					.flatMap(errorBody -> {
						log.error("Toss Payment 승인 실패: status={}, body={}", response.statusCode(), errorBody);
						return Mono.error(new RuntimeException("Payment confirm failed: " + errorBody));
					});
			})
			.bodyToMono(TossPaymentConfirmResponse.class)
			.block(); // Service Layer에서 동기적으로 처리하기 위해 block 사용
	}
}
