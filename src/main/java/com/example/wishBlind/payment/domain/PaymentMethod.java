package com.example.wishBlind.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 결제 수단(mock). */
@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

    CARD("카드"),
    EASY_PAY("간편결제"),
    BANK_TRANSFER("계좌이체");

    private final String label;
}
