package com.ongi.api.user.application;

import com.ongi.api.common.web.dto.JwtTokens;
import com.ongi.api.config.auth.JwtTokenProvider;
import com.ongi.api.config.cache.UserRedisTemplate;
import com.ongi.api.config.cache.store.user.EmailVerificationStore;
import com.ongi.api.config.cache.store.user.SignUpTokenStore;
import com.ongi.api.user.application.component.MailSender;
import com.ongi.api.config.cache.store.user.PasswordResetTokenStore;
import com.ongi.api.config.cache.store.user.RefreshTokenStore;
import com.ongi.api.config.properties.JwtProperties;
import com.ongi.api.user.persistence.UserAdapter;
import com.ongi.api.user.web.dto.EmailVerifyConfirmRequest;
import com.ongi.api.user.web.dto.EmailVerifyRequest;
import com.ongi.api.user.web.dto.FindEmailRequest;
import com.ongi.api.user.web.dto.MemberSignUpRequest;
import com.ongi.api.user.web.dto.MemberLoginRequest;
import com.ongi.api.user.web.dto.PasswordResetConfirmRequest;
import com.ongi.api.user.web.dto.PasswordResetRequest;
import com.ongi.api.user.web.dto.SignUpTokenResponse;
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
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

	private final FileService fileService;

	private final UserAdapter userAdapter;

	private final PasswordEncoder passwordEncoder;

	private final EmailVerificationStore emailVerificationStore;

	private final SignUpTokenStore signUpTokenStore;

	private final RefreshTokenStore refreshStore;

	private final PasswordResetTokenStore passwordResetTokenStore;

	private final JwtTokenProvider jwtTokenProvider;

	private final JwtProperties props;

	private final MailSender mailSender;

	private static final Duration EMAIL_CODE_TTL = Duration.ofMinutes(10);

	private static final Duration SIGNUP_TOKEN_TTL = Duration.ofMinutes(15);

	private static final long MAX_VERIFY_ATTEMPTS = 5;

	private static final Duration RESET_TTL = Duration.ofMinutes(15);

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public void requestEmailVerification(EmailVerifyRequest req) {
		String email = req.email();

		if (userAdapter.existsUserByEmail(email)) {
			return;
		}

		String code = generate6DigitCode();
		String codeHash = UserRedisTemplate.sha256(code);

		emailVerificationStore.putCodeHash(email, codeHash, EMAIL_CODE_TTL);
		mailSender.sendEmailVerificationCode(email, code);
	}

	private String generate6DigitCode() {
		int n = ThreadLocalRandom.current().nextInt(0, 1_000_000);
		return String.format("%06d", n);
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public SignUpTokenResponse confirmEmailVerification(EmailVerifyConfirmRequest req) {
		String email = req.email();

		// 가입된 이메일 막음
		if (userAdapter.existsUserByEmail(email)) {
			throw new IllegalArgumentException("Already registered");
		}

		String storedHash = emailVerificationStore.getCodeHash(email);
		if (storedHash == null) {
			throw new IllegalArgumentException("Invalid or expired code");
		}

		long attempts = emailVerificationStore.incrAttempt(email);
		if (attempts > MAX_VERIFY_ATTEMPTS) {
			emailVerificationStore.clear(email);
			throw new IllegalArgumentException("Too many attempts");
		}

		String inputHash = UserRedisTemplate.sha256(req.code());
		if (!storedHash.equals(inputHash)) {
			throw new IllegalArgumentException("Invalid or expired code");
		}

		// 성공: 인증코드 폐기(재사용 방지)
		emailVerificationStore.clear(email);

		// signupToken 발급(원문은 클라에, 저장은 해시로)
		String rawSignupToken = UUID.randomUUID().toString();
		String tokenHash = UserRedisTemplate.sha256(rawSignupToken);

		signUpTokenStore.put(tokenHash, email, SIGNUP_TOKEN_TTL);

		return new SignUpTokenResponse(rawSignupToken);
	}

	@Transactional(transactionManager = "transactionManager")
	public void signUp(MemberSignUpRequest request) {

		// signupToken 검증
		if (request.signUpToken() == null || request.signUpToken().isBlank()) {
			throw new IllegalArgumentException("signupToken required");
		}

		String tokenHash = UserRedisTemplate.sha256(request.signUpToken());
		String emailFromToken = signUpTokenStore.getEmail(tokenHash);

		if (emailFromToken == null) {
			throw new IllegalArgumentException("Invalid or expired signupToken");
		}
		if (!emailFromToken.equals(request.email())) {
			throw new IllegalArgumentException("signupToken does not match email");
		}

		// 재사용 방지: 토큰 먼저 consume (동시성 대응)
		signUpTokenStore.consume(tokenHash);

		// 실제 가입 처리
		if (userAdapter.existsUserByEmail(request.email())) {
			throw new IllegalArgumentException("Email already exists");
		}

		User user = User.create(
			null,
			request.email(),
			passwordEncoder.encode(request.password()),
			UserTypeEnum.EMAIL,
			UserTier.USER
		);
		user = userAdapter.save(user);

		String finalProfileImageKey = null;
		if (request.profileImageUploadToken() != null && request.profileImageObjectKey() != null) {
			finalProfileImageKey = fileService.consumeAndPromoteProfileImage(
				request.profileImageUploadToken(),  // UUID
				request.profileImageObjectKey(),    // tmp objectKey
				user.getId()
			);
		}

		UserProfile userProfile = UserProfile.create(
			user.getId(),
			request.displayName(),
			finalProfileImageKey
		);
		userAdapter.save(userProfile);
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
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

	@Transactional(transactionManager = "transactionManager", readOnly = true)
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

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public void findEmail(FindEmailRequest req) {
		userAdapter.findUserProfileByDisplayName(req.displayName())
			.ifPresent(user -> {
				 mailSender.sendUserIdGuideMail(req.email());
			});
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public void requestPasswordReset(PasswordResetRequest req) {
		userAdapter.findUserByEmail(req.email()).ifPresent(user -> {
			String rawToken = UUID.randomUUID().toString();
			String tokenHash = UserRedisTemplate.sha256(rawToken);

			passwordResetTokenStore.put(tokenHash, String.valueOf(user.getId()), RESET_TTL);

			// TODO Front URL 로 교체
			String link = "https://your-frontend/reset-password?token=" + rawToken;
			mailSender.sendPasswordResetMail(user.getEmail(), link);
		});
	}

	@Transactional(transactionManager = "transactionManager")
	public void confirmPasswordReset(PasswordResetConfirmRequest req) {
		String tokenHash = UserRedisTemplate.sha256(req.token());
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
