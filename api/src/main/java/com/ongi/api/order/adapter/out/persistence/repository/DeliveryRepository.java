package com.ongi.api.order.adapter.out.persistence.repository;

import com.ongi.api.order.adapter.out.persistence.DeliveryEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long> {
	Optional<DeliveryEntity> findByOrderId(Long orderId);
}
