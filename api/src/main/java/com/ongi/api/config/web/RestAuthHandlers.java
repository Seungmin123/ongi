package com.ongi.api.config.web;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

public class RestAuthHandlers {

	public static AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (request, response, authException) -> writeJson(
			response, 401, "{\"code\":\"UNAUTHORIZED\",\"message\":\"Authentication required\"}"
		);
	}

	public static AccessDeniedHandler accessDeniedHandler() {
		return (request, response, accessDeniedException) -> writeJson(
			response, 403, "{\"code\":\"FORBIDDEN\",\"message\":\"Access denied\"}"
		);
	}

	private static void writeJson(HttpServletResponse response, int status, String body) throws IOException {
		response.setStatus(status);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(body);
	}
}