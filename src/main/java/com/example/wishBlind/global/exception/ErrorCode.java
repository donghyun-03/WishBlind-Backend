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
    RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "D003", "추천을 찾을 수 없습니다."),

    // V = 전달(deliVery)
    DELIVERY_INFO_REQUIRED(HttpStatus.BAD_REQUEST, "V001", "선택한 전달 방법에 필요한 정보가 누락되었습니다."),
    NOT_FINALIZED(HttpStatus.BAD_REQUEST, "V002", "최종 상품을 선택한 뒤에 전달 정보를 입력할 수 있습니다."),
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "V003", "전달 정보를 찾을 수 없습니다."),

    // A = 인증/회원
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
    // 이메일 없음과 비밀번호 틀림을 구분하지 않는다. 구분하면 가입 여부가 노출된다.
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A004", "이메일 또는 비밀번호가 올바르지 않습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "A005", "이미 사용 중인 이메일입니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "A006", "필수 약관에 동의해야 합니다."),
    SOCIAL_ACCOUNT_ALREADY_LINKED(HttpStatus.CONFLICT, "A007", "이미 다른 회원에게 연결된 소셜 계정입니다."),
    SOCIAL_PROVIDER_ALREADY_LINKED(HttpStatus.CONFLICT, "A008", "이미 연결된 소셜 제공자입니다."),
    OAUTH_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "A009", "소셜 로그인 검증에 실패했습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A010", "회원을 찾을 수 없습니다."),
    WITHDRAWN_USER(HttpStatus.FORBIDDEN, "A011", "탈퇴한 회원입니다."),

    // L = AI(LLM) 연결
    LLM_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "L001", "AI 분석이 시간 내에 끝나지 않았습니다."),
    LLM_PARSE_ERROR(HttpStatus.BAD_GATEWAY, "L002", "AI 응답 형식이 올바르지 않습니다."),
    LLM_REFUSAL(HttpStatus.UNPROCESSABLE_CONTENT, "L003", "AI가 요청을 처리할 수 없습니다."),
    LLM_INVALID_PICK(HttpStatus.UNPROCESSABLE_CONTENT, "L004", "조건을 만족하는 추천을 만들지 못했습니다."),
    LLM_NO_CANDIDATE(HttpStatus.BAD_REQUEST, "L005", "추천할 후보 상품이 없습니다."),
    LLM_CALL_FAILED(HttpStatus.BAD_GATEWAY, "L006", "AI 호출에 실패했습니다.");

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
