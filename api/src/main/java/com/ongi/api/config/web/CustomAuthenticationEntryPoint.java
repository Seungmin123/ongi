package com.ongi.api.config.web;

import com.ongi.api.config.exception.CommonException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

// TODO Error Response에 맞춰 수정

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");

		String responseBody;

		if (authException instanceof JwtAuthenticationException) {
			JwtAuthenticationException jwtEx = (JwtAuthenticationException) authException;
			CommonException wrapped = jwtEx.getWrappedException();
			response.setStatus(wrapped.getStatus().value());
			responseBody = ErrorResponseBuilder.build(wrapped);
		} else {
			responseBody = ErrorResponseBuilder.buildFallback();
		}

		response.getWriter().write(responseBody);
	}
}
