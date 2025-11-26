package com.ongi.api.config.web;

import com.ongi.api.config.exception.CommonException;
import java.util.HashMap;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;

public class ErrorResponseBuilder {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static String build(CommonException ex) {
		Map<String, Object> response = new HashMap<>();
		response.put("code", ex.getCode());
		response.put("message", ex.getMessage());
		response.put("data", ex.getData());

		try {
			return objectMapper.writeValueAsString(response);
		} catch (Exception e) {
			return "{\"code\": \"99999\", \"message\": \"Internal error while building error response.\", \"data\": null}";
		}
	}

	public static String buildFallback() {
		return "{\"code\": \"401\", \"message\": \"Unauthorized\", \"data\": null}";
	}
}
