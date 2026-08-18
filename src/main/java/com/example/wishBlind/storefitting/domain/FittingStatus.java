package com.example.wishBlind.storefitting.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 매장 체험 예약 상태. */
@Getter
@RequiredArgsConstructor
public enum FittingStatus {

    WAITING("체험 대기"),
    IN_PROGRESS("체험 중"),
    DONE("체험 완료"),
    CANCELED("취소");

    private final String label;
}
