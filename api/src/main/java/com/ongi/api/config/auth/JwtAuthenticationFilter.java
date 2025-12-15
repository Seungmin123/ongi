package com.ongi.api.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		// 이미 인증된 요청이면 그대로 통과 (중복 세팅 방지)
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = resolveBearerToken(request);
		if (token == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			// Access 토큰만 검증 (accessSecret)
			var jws = jwtTokenProvider.parseAccess(token);
			String userId = jws.getBody().getSubject();

			var authorities = jwtTokenProvider.toAuthoritiesFromAccessToken(token);

			var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (Exception e) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null) return null;
		if (!header.startsWith("Bearer ")) return null;
		String token = header.substring(7).trim();
		return token.isEmpty() ? null : token;
	}

}
