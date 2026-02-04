package com.ongi.api.order.adapter.out.persistence.repository;

import com.ongi.api.order.adapter.out.persistence.OrderEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
	Optional<OrderEntity> findByOrderNumber(String orderNumber);
}
