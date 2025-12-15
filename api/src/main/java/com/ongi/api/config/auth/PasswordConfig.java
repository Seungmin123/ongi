package com.ongi.api.config.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		// 기본 강도(10)도 충분하지만 트래픽/보안 수준에 맞게 조절 가능
		return new BCryptPasswordEncoder();
	}
}