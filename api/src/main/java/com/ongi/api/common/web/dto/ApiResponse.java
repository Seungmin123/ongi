package com.ongi.api.common.web.dto;

public record ApiResponse<T> (
	boolean success,  // 요청 성공 여부
	T data,           // 실제 응답 데이터
	ErrorResponse error, // 에러 발생 시 정보
	Meta meta
) {
	public ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, data, null, null);
	}

	public ApiResponse<T> okWithMeta(T data, Meta meta) {
		return new ApiResponse<>(true, data, null, meta);
	}

	public ApiResponse<T> okWithError(T data, ErrorResponse error) {
		return new ApiResponse<>(true, data, error, null);
	}

	public ApiResponse<T> error(String code, String message) {
		return new ApiResponse<>(false, null, new ErrorResponse(code, message), null);
	}

}
