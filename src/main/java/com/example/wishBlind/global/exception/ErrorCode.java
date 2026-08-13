package com.example.wishBlind.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 서비스 전역 에러 코드.
 * 접두 C = 공통. 도메인별 코드(G=선물세션, I=초대, R=추천 등)는 기능 구현 시 추가한다.
 */
public enum ErrorCode {

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "예상치 못한 서버 오류가 발생했습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C002", "요청 값이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C004", "허용되지 않은 요청 방식입니다."),

    // G = 선물 세션
    GIFT_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "G001", "선물 세션을 찾을 수 없습니다."),

    // I = 초대
    INVALID_INVITE(HttpStatus.BAD_REQUEST, "I001", "유효하지 않은 초대입니다."),

    // R = 받는 사람 취향
    PREFERENCE_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "R001", "이미 취향을 입력한 초대입니다."),

    // P = 상품
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "상품을 찾을 수 없습니다."),

    // D = 추천(recommenDation)
    PREFERENCE_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "D001", "받는 사람이 아직 취향을 입력하지 않았습니다."),
    NO_CANDIDATE(HttpStatus.UNPROCESSABLE_ENTITY, "D002", "조건에 맞는 상품이 없습니다. 예산이나 카테고리를 조정해 주세요."),
    RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "D003", "추천을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
