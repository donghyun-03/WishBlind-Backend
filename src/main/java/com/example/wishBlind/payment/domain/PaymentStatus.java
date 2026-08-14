package com.example.wishBlind.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 결제 상태(mock). */
@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    READY("결제 대기"),
    PAID("결제 완료"),
    FAILED("결제 실패"),
    CANCELED("결제 취소");

    private final String label;
}
