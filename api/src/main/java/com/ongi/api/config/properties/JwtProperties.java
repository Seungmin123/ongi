package com.ongi.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.jwt")
public record JwtProperties (
	String issuer,
	long accessExpSeconds,
	long refreshExpSeconds,
	String accessKey,
	String refreshKey
) {}
