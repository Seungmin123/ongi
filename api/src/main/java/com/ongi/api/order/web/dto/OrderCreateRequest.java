package com.ongi.api.order.web.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderCreateRequest(
	@NotEmpty
	List<OrderItemRequest> items,
	String address,
	String receiverName,
	String receiverPhone
) {}
