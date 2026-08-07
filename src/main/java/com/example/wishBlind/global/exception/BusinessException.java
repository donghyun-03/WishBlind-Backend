package com.example.wishBlind.global.exception;

import lombok.Getter;

/**
 * 비즈니스 로직에서 던지는 예외.
 * ErrorCode를 담아 GlobalExceptionHandler가 일괄 처리한다.
 * 예) throw new BusinessException(ErrorCode.NOT_FOUND);
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
