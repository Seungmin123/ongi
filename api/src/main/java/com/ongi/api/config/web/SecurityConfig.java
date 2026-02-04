package com.ongi.api.config.web;

import com.ongi.api.config.auth.JwtAuthenticationFilter;
import com.ongi.api.config.auth.RestAuthHandlers;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	// TODO 수정
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.httpBasic(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(eh -> eh
				.authenticationEntryPoint(RestAuthHandlers.unauthorizedEntryPoint())
				.accessDeniedHandler(RestAuthHandlers.accessDeniedHandler()))
			.headers(h -> h
				.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; img-src 'self' data:;"))
				.frameOptions(FrameOptionsConfig::disable)
			)
			.authorizeHttpRequests(auth -> auth
				// TODO 수정 필요
				.requestMatchers("/recipe/public/**", "/user/public/**", "/file/public/**", "/community/public/**")
				.permitAll()
				.requestMatchers("/recipe/private/**", "/user/private/**", "/file/private/**", "/order/private/**", "/community/private/**")
				.authenticated()
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public HttpFirewall allowUrlEncodedPercentHttpFirewall() {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowUrlEncodedPercent(true);
		return firewall;
	}

}