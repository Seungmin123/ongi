package com.ongi.api.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import tools.jackson.databind.ObjectMapper;

@Order(0)
@Configuration
public class WebConfig {

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

}
