package com.ongi.api.user.application;

import com.ongi.api.common.web.dto.JwtTokens;
import com.ongi.api.config.auth.JwtTokenProvider;
import com.ongi.api.user.application.component.MailSender;
import com.ongi.api.user.cache.PasswordResetTokenStore;
import com.ongi.api.user.cache.RefreshTokenStore;
import com.ongi.api.config.properties.JwtProperties;
import com.ongi.api.user.persistence.UserAdapter;
import com.ongi.api.user.web.dto.FindEmailRequest;
import com.ongi.api.user.web.dto.MemberJoinRequest;
import com.ongi.api.user.web.dto.MemberLoginRequest;
import com.ongi.api.user.web.dto.PasswordResetConfirmRequest;
import com.ongi.api.user.web.dto.PasswordResetRequest;
import com.ongi.user.domain.User;
import com.ongi.user.domain.UserProfile;
import com.ongi.user.domain.UserRoleResolver;
import com.ongi.user.domain.enums.UserTier;
import com.ongi.user.domain.enums.UserTypeEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

	private final UserAdapter userAdapter;

	private final PasswordEncoder passwordEncoder;

	private final RefreshTokenStore refreshStore;

	private final PasswordResetTokenStore passwordResetTokenStore;

	private final JwtTokenProvider jwtTokenProvider;

	private final JwtProperties props;

	private final MailSender mailSender;

	private static final Duration RESET_TTL = Duration.ofMinutes(15);

	@Transactional(transactionManager = "transactionManager")
	public void join(MemberJoinRequest request) {
		if (userAdapter.existsUserByEmail(request.email())) {
			throw new IllegalArgumentException("Email already exists");
		}

		User user = User.create(null, request.email(), passwordEncoder.encode(request.password()), UserTypeEnum.EMAIL, UserTier.USER);
		user = userAdapter.save(user);

		UserProfile userProfile = UserProfile.create(user.getId(), request.displayName(), request.profileImageUrl());
		userAdapter.save(userProfile);
	}

	@Transactional(readOnly = true)
    public JwtTokens login(MemberLoginRequest req) {
        User user = userAdapter.findUserByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
	        // TODO Custom Error
            throw new IllegalArgumentException("Invalid credentials");
        }

        String userId = String.valueOf(user.getId());

		String[] roles = UserRoleResolver.getRoles(user);

		Map<String, Object> accessClaims = Map.of(
			"roles", roles,
			"tier", user.getTier().name()
		);

        String access = jwtTokenProvider.createAccessToken(userId, accessClaims);

        String jti = UUID.randomUUID().toString();
        String refresh = jwtTokenProvider.createRefreshToken(userId, jti);

        refreshStore.put(userId, jti, Duration.ofSeconds(props.refreshExpSeconds()));
        return new JwtTokens(access, refresh);
    }

	@Transactional
	public JwtTokens refresh(String refreshToken) {
		Jws<Claims> jws = jwtTokenProvider.parseRefresh(refreshToken);
		Claims claims = jws.getBody();

		String userId = claims.getSubject();
		String jti = claims.getId();

		// 재사용(탈취) 탐지: 이미 폐기된 jti면 공격 가능성 높음
		String storedUserId = refreshStore.getUserIdByJti(jti);
		if (storedUserId == null) {
			// 강하게 대응: 해당 유저 전체 세션 폐기
			refreshStore.revokeAll(userId);
			// TODO Custom Error
			throw new IllegalArgumentException("Refresh token reused or revoked");
		}
		if (!storedUserId.equals(userId)) {
			refreshStore.revokeAll(userId);
			// TODO Custom Error
			throw new IllegalArgumentException("Refresh token invalid");
		}

		// 로테이션: 기존 refresh 폐기
		refreshStore.removeJti(userId, jti);

		// 최신 권한 반영
		User user = userAdapter.findUserById(Long.valueOf(userId))
			.orElseThrow(() -> new IllegalStateException("User not found"));

		String[] roles = UserRoleResolver.getRoles(user);

		Map<String, Object> accessClaims = Map.of(
			"roles", roles,
			"tier", user.getTier().name()
		);

		String newAccess = jwtTokenProvider.createAccessToken(userId, accessClaims);

		String newJti = UUID.randomUUID().toString();
		String newRefresh = jwtTokenProvider.createRefreshToken(userId, newJti);
		refreshStore.put(userId, newJti, Duration.ofSeconds(props.refreshExpSeconds()));

		return new JwtTokens(newAccess, newRefresh);

	}

	@Transactional(readOnly = true)
	public void findEmail(FindEmailRequest req) {
		userAdapter.findUserProfileByDisplayName(req.displayName())
			.ifPresent(user -> {
				 mailSender.sendUserIdGuideMail(req.email());
			});
	}

	@Transactional
	public void requestPasswordReset(PasswordResetRequest req) {
		userAdapter.findUserByEmail(req.email()).ifPresent(user -> {
			String rawToken = UUID.randomUUID().toString();
			String tokenHash = PasswordResetTokenStore.sha256(rawToken);

			passwordResetTokenStore.put(tokenHash, String.valueOf(user.getId()), RESET_TTL);

			// TODO Front URL 로 교체
			String link = "https://your-frontend/reset-password?token=" + rawToken;
			mailSender.sendPasswordResetMail(user.getEmail(), link);
		});
	}

	@Transactional
	public void confirmPasswordReset(PasswordResetConfirmRequest req) {
		String tokenHash = PasswordResetTokenStore.sha256(req.token());
		String userId = passwordResetTokenStore.getUserId(tokenHash);

		if (userId == null) {
			throw new IllegalArgumentException("Invalid or expired reset token");
		}

		// 1회용 소비
		passwordResetTokenStore.consume(tokenHash);

		User user = userAdapter.findUserById(Long.valueOf(userId))
			.orElseThrow(() -> new IllegalStateException("User not found"));

		userAdapter.updatePasswordHash(user.getId(), passwordEncoder.encode(req.newPassword()));

		// 비번 바뀌면 기존 refresh 전부 폐기
		refreshStore.revokeAll(userId);
	}
}
