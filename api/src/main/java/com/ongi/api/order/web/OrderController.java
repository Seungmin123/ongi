package com.ongi.api.order.web;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.common.web.dto.AuthPrincipal;
import com.ongi.api.order.application.command.OrderService;
import com.ongi.api.order.web.dto.OrderCreateRequest;
import com.ongi.api.order.web.dto.PaymentConfirmRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/order")
@RestController
public class OrderController {

	private final OrderService orderService;

	/**
	 * 주문 생성 및 재고 선차감
	 */
	@PostMapping("/private/order")
	public ApiResponse<String> createOrder(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@RequestBody @Valid OrderCreateRequest request
	) {
		return ApiResponse.ok(orderService.createOrder(authPrincipal.userId(), request));
	}

	/**
	 * 결제 최종 승인 요청 (외부 PG 호출 포함)
	 */
	@PostMapping("/private/payment/confirm")
	public ApiResponse<Void> confirmPayment(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@RequestBody @Valid PaymentConfirmRequest request
	) {
		orderService.completePayment(request.orderNumber(), request.paymentKey());
		return ApiResponse.ok();
	}
}
