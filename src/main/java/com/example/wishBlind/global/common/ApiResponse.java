package com.example.wishBlind.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 모든 API 응답의 통일 포맷.
 * 성공: { "success": true,  "data": {...}, "error": null }
 * 실패: { "success": false, "data": null,  "error": { "code": "...", "message": "..." } }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, ErrorBody error) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /** 반환 데이터가 없는 성공 응답 */
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message));
    }

    /** 에러 상세 (코드 + 메시지) */
    public record ErrorBody(String code, String message) {
    }
}
