package com.ongi.api.user.web;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.common.web.dto.JwtTokens;
import com.ongi.api.user.application.AuthService;
import com.ongi.api.user.application.UserService;
import com.ongi.api.user.web.dto.EmailVerifyConfirmRequest;
import com.ongi.api.user.web.dto.EmailVerifyRequest;
import com.ongi.api.user.web.dto.FindEmailRequest;
import com.ongi.api.user.web.dto.MemberSignUpRequest;
import com.ongi.api.user.web.dto.MemberLoginRequest;
import com.ongi.api.user.web.dto.MyPageResponse;
import com.ongi.api.user.web.dto.PasswordResetConfirmRequest;
import com.ongi.api.user.web.dto.PasswordResetRequest;
import com.ongi.user.domain.enums.MeInclude;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
public class UserController {

	private final UserService userService;

	private final AuthService authService;

	/**
	 * 이메일 회원가입 - 이메일 인증 요청
 	 * @param req
	 * @return
	 */
	@PostMapping("/public/signup/email-request")
	public ApiResponse<Void> signUpEmailRequest(
		@RequestBody EmailVerifyRequest req
	) {
		authService.requestEmailVerification(req);
		return ApiResponse.ok();
	}

	/**
	 * 이메일 회원가입 - 이메일 인증 확인
	 * @param req
	 * @return
	 */
	@PostMapping("/public/signup/email-confirm")
	public ApiResponse<Void> signUpEmailConfirm(
		@RequestBody EmailVerifyConfirmRequest req
	) {
		authService.confirmEmailVerification(req);
		return ApiResponse.ok();
	}

	/**
	 * 이메일 회원가입
	 * @param memberSignUpRequest
	 * @return
	 */
	@PostMapping("/public/signup")
	public ApiResponse<Void> signUp(
		@RequestBody MemberSignUpRequest memberSignUpRequest
	) {
		authService.signUp(memberSignUpRequest);
		return ApiResponse.ok();
	}

	/**
	 * 이메일 로그인
	 * @param req
	 * @return
	 */
	@PostMapping("/public/login")
	public ApiResponse<JwtTokens> login(
		@RequestBody MemberLoginRequest req
	) {
		return ApiResponse.ok(authService.login(req));
	}

	/**
	 * 아이디 찾기
	 * @param findEmailRequest
	 * @return
	 */
	@GetMapping("/public/id/find")
	public ApiResponse<Void> findById(
		@ModelAttribute FindEmailRequest findEmailRequest
	) {
		authService.findEmail(findEmailRequest);
		return ApiResponse.ok();
	}

	/**
	 * 비밀번호 재설정 메일 전송
	 * @param passwordResetRequest
	 * @return
	 */
	@PostMapping("/public/password/reset-request")
	public ApiResponse<Void> resetPassword(
		@RequestBody PasswordResetRequest passwordResetRequest
	) {
		authService.requestPasswordReset(passwordResetRequest);
		return ApiResponse.ok();
	}

	/**
	 * 비밀번호 재설정 메일 확인
	 * @param passwordResetConfirmRequest
	 * @return
	 */
	@PostMapping("/public/password/reset-confirm")
	public ApiResponse<Void> resetPasswordConfirm(
		@RequestBody PasswordResetConfirmRequest passwordResetConfirmRequest
	) {
		authService.confirmPasswordReset(passwordResetConfirmRequest);
		return ApiResponse.ok();
	}

	@GetMapping("/private/me")
	public ApiResponse<MyPageResponse> me(
		Authentication authentication,
		@RequestParam Set<String> include
	) {
		Long userId = Long.parseLong(authentication.getPrincipal().toString());
		Set<MeInclude> includes = include.stream()
			.map(String::toUpperCase)
			.map(MeInclude::valueOf)
			.collect(Collectors.toCollection(() -> EnumSet.noneOf(MeInclude.class)));

		return ApiResponse.ok(userService.getMe(userId, includes));
	}

	@PatchMapping("/private/me/summary")
	public ApiResponse<Void> updateSummary() {
		return ApiResponse.ok();
	}

	@PatchMapping("/private/me/basic")
	public ApiResponse<Void> updateBasic() {
		return ApiResponse.ok();
	}

	@PatchMapping("/private/me/personalization")
	public ApiResponse<Void> updatePersonalization() {
		return ApiResponse.ok();
	}

	@GetMapping("/private/me/stats")
	public ApiResponse<Void> meStats() {
		return ApiResponse.ok();
	}

	// TODO OAuth Naver

	// TODO OAuth Kakao

	// TODO 내 프로필 정보

	// TODO 내 프로필 정보 수정


	// User(Community) -------------------

	// TODO 댓글 리스트

	// TODO 댓글 쓰기

	// TODO 댓글 수정

	// TODO 좋아요

	// TODO 저장하기

	// TODO 내 활동 통계
}
