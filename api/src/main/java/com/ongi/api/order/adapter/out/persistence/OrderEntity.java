package com.ongi.api.order.adapter.out.persistence;

import com.ongi.order.domain.enums.OrderStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class OrderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	@Column(unique = true)
	private String orderNumber;

	private Long totalPrice;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	private LocalDateTime orderedAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItemEntity> orderItems = new ArrayList<>();

	@Builder
	public OrderEntity(Long userId, String orderNumber, Long totalPrice, OrderStatus status, LocalDateTime orderedAt) {
		this.userId = userId;
		this.orderNumber = orderNumber;
		this.totalPrice = totalPrice;
		this.status = status;
		this.orderedAt = orderedAt;
	}

	public void addOrderItem(OrderItemEntity orderItem) {
		this.orderItems.add(orderItem);
		orderItem.setOrder(this);
	}

	public void updateStatus(OrderStatus status) {
		this.status = status;
	}
}
