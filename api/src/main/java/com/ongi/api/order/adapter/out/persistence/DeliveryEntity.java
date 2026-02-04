package com.ongi.api.order.adapter.out.persistence;

import com.ongi.order.domain.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "delivery")
public class DeliveryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long orderId;

	private String trackingNumber;

	private String courier;

	@Enumerated(EnumType.STRING)
	private DeliveryStatus status;

	private String address;

	private String receiverName;

	private String receiverPhone;

	@Builder
	public DeliveryEntity(Long orderId, String address, String receiverName, String receiverPhone) {
		this.orderId = orderId;
		this.address = address;
		this.receiverName = receiverName;
		this.receiverPhone = receiverPhone;
		this.status = DeliveryStatus.READY;
	}

	public void updateStatus(DeliveryStatus status) {
		this.status = status;
	}
}
