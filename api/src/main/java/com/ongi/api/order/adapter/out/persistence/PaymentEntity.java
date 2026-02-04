package com.ongi.api.order.adapter.out.persistence;

import com.ongi.order.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payment")
public class PaymentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long orderId;

	private String paymentKey;

	private String method;

	private Long amount;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	private LocalDateTime approvedAt;

	@Builder
	public PaymentEntity(Long orderId, String paymentKey, String method, Long amount, PaymentStatus status, LocalDateTime approvedAt) {
		this.orderId = orderId;
		this.paymentKey = paymentKey;
		this.method = method;
		this.amount = amount;
		this.status = status;
		this.approvedAt = approvedAt;
	}

	public void complete(String paymentKey, LocalDateTime approvedAt) {
		this.paymentKey = paymentKey;
		this.status = PaymentStatus.DONE;
		this.approvedAt = approvedAt;
	}
}
