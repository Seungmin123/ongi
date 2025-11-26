package com.ongi.api.config.web;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

	// TODO 수정
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.httpBasic(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthenticationEntryPoint))
			.headers(h -> h
				.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; img-src 'self' data:;"))
				.frameOptions(FrameOptionsConfig::disable)
			)
			.authorizeHttpRequests(auth -> auth
				// TODO 수정 필요
				.requestMatchers("/public/**")
				.permitAll()
				.requestMatchers("/private/**")
				.permitAll()
//				.requestMatchers("/v1/user/checkTag", // 체크 태그
//					"/v1/user/mic/processed", "/v1/user/mic", "/v1/user/version", // 버전 정보, 마이크 정보
//					"/v1/user/kit/**", "/v1/user/url", // 키트 삭제, URL 조회
//					"/v1/content/list")
//				.hasAnyRole("GUEST", "HALF_LINKER", "LINKER", "ENGINEER")
//				.requestMatchers("/v1/**")
//				.hasAnyRole("HALF_LINKER", "LINKER", "ENGINEER")
//				.anyRequest().authenticated()
			);

		return http.build();
	}

	@Bean
	public HttpFirewall allowUrlEncodedPercentHttpFirewall() {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowUrlEncodedPercent(true);
		return firewall;
	}

}