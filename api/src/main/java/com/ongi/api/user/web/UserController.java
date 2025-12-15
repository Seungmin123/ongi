package com.ongi.api.user.web;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.user.application.UserService;
import com.ongi.api.user.web.dto.MemberJoinRequest;
import com.ongi.api.user.web.dto.MemberResponse;
import com.ongi.user.domain.enums.PresignedTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
public class UserController {

	private final UserService userService;

	// TODO 이미지 업로드 기능 구현
	@GetMapping("/public/image/pre-signed/{imageType}")
	public ApiResponse<Void> imagePreSigned(
		@PathVariable String imageType
	) {
		PresignedTypeEnum presignedTypeEnum = PresignedTypeEnum.from(imageType);
		if(presignedTypeEnum == null) {
			throw new IllegalArgumentException("Invalid image type: " + imageType);
		}
		return ApiResponse.ok();
	}


	/**
	 * 이메일 회원가입
	 * @param memberJoinRequest
	 * @return
	 */
	@PostMapping("/public/join")
	public ApiResponse<Void> join(
		@RequestBody MemberJoinRequest memberJoinRequest
	) {
		userService.join(memberJoinRequest);
		return ApiResponse.ok();
	}

	// TODO Email 발송

	// TODO 아이디 찾기

	// TODO 비밀번호 재설정

	// TODO 로그인

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
