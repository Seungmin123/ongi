package com.ongi.api.order.application.command;

import com.ongi.api.common.lock.DistributedLockExecutor;
import com.ongi.api.order.adapter.out.persistence.DeliveryEntity;
import com.ongi.api.order.adapter.out.persistence.OrderEntity;
import com.ongi.api.order.adapter.out.persistence.OrderItemEntity;
import com.ongi.api.order.adapter.out.persistence.PaymentEntity;
import com.ongi.api.order.adapter.out.persistence.repository.DeliveryRepository;
import com.ongi.api.order.adapter.out.persistence.repository.OrderRepository;
import com.ongi.api.order.adapter.out.persistence.repository.PaymentRepository;
import com.ongi.api.order.application.port.PaymentClient;
import com.ongi.api.order.messaging.event.OrderPaidEvent;
import com.ongi.api.order.messaging.producer.OrderEventProducer;
import com.ongi.api.order.web.dto.OrderCreateRequest;
import com.ongi.api.order.web.dto.OrderItemRequest;
import com.ongi.api.product.adapter.out.persistence.ProductEntity;
import com.ongi.api.product.adapter.out.persistence.repository.ProductRepository;
import com.ongi.order.domain.enums.DeliveryStatus;
import com.ongi.order.domain.enums.OrderStatus;
import com.ongi.order.domain.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final DeliveryRepository deliveryRepository;
	private final ProductRepository productRepository;
	private final DistributedLockExecutor lockExecutor;
	private final OrderEventProducer eventProducer;
	private final PaymentClient paymentClient;

	public String createOrder(Long userId, OrderCreateRequest request) {
		// 1. 주문 전체 상품들에 대해 락 키 생성 (정렬하여 데드락 방지)
		List<Long> productIds = request.items().stream()
			.map(OrderItemRequest::productId)
			.sorted()
			.toList();

		String lockKey = "lock:order:products:" + productIds.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));

		// 2. 분산 락 실행기 사용
		return lockExecutor.execute(lockKey, 5, 10, () -> createOrderInternal(userId, request));
	}

	@Transactional(transactionManager = "transactionManager")
	protected String createOrderInternal(Long userId, OrderCreateRequest request) {
		// 1. 재고 차감
		List<Long> productIds = request.items().stream().map(item -> item.productId()).toList();
		List<ProductEntity> products = productRepository.findAllById(productIds);

		Map<Long, ProductEntity> productMap = products.stream()
			.collect(Collectors.toMap(ProductEntity::getId, p -> p));

		request.items().forEach(itemReq -> {
			ProductEntity product = productMap.get(itemReq.productId());
			if (product == null) throw new IllegalArgumentException("상품을 찾을 수 없습니다: " + itemReq.productId());
			product.decreaseStock(itemReq.quantity());
		});

		// 2. 주문 생성
		String orderNumber = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		long totalPrice = request.items().stream()
			.mapToLong(item -> item.price() * item.quantity())
			.sum();

		OrderEntity order = OrderEntity.builder()
			.userId(userId)
			.orderNumber(orderNumber)
			.totalPrice(totalPrice)
			.status(OrderStatus.PENDING)
			.orderedAt(LocalDateTime.now())
			.build();

		request.items().forEach(itemReq -> {
			order.addOrderItem(OrderItemEntity.builder()
				.productId(itemReq.productId())
				.productName(itemReq.productName())
				.price(itemReq.price())
				.quantity(itemReq.quantity())
				.build());
		});
		orderRepository.save(order);

		// 3. 결제 및 배송 초기 정보
		paymentRepository.save(PaymentEntity.builder()
			.orderId(order.getId())
			.amount(totalPrice)
			.status(PaymentStatus.READY)
			.build());

		deliveryRepository.save(DeliveryEntity.builder()
			.orderId(order.getId())
			.address(request.address())
			.receiverName(request.receiverName())
			.receiverPhone(request.receiverPhone())
			.build());

		return orderNumber;
	}

	@Transactional(transactionManager = "transactionManager")
	public void completePayment(String orderNumber, String paymentKey) {
		// 1. 주문 조회
		OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		// 2. 외부 PG사 승인 요청
		paymentClient.confirm(paymentKey, orderNumber, order.getTotalPrice());

		// 3. 내부 결제 상태 업데이트
		PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
			.orElseThrow(() -> new IllegalStateException("결제 정보를 찾을 수 없습니다."));

		LocalDateTime now = LocalDateTime.now();
		payment.complete(paymentKey, now);
		order.updateStatus(OrderStatus.PAID);

		// 4. 배송 지시
		DeliveryEntity delivery = deliveryRepository.findByOrderId(order.getId())
			.orElseThrow(() -> new IllegalStateException("배송 정보를 찾을 수 없습니다."));
		delivery.updateStatus(DeliveryStatus.IN_PROGRESS);

		// 5. 이벤트 발행
		eventProducer.sendPaidEvent(new OrderPaidEvent(
			order.getId(),
			order.getUserId(),
			order.getTotalPrice(),
			now
		));
	}
}
