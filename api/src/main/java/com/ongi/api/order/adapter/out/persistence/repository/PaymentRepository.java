package com.ongi.api.order.adapter.out.persistence.repository;

import com.ongi.api.order.adapter.out.persistence.PaymentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
	Optional<PaymentEntity> findByOrderId(Long orderId);
	Optional<PaymentEntity> findByPaymentKey(String paymentKey);
}
