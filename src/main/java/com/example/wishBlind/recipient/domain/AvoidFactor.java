package com.example.wishBlind.recipient.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 피하고 싶은 요소 (STEP 05, 복수 선택). */
@Getter
@RequiredArgsConstructor
public enum AvoidFactor {

    BIG_LOGO("큰 로고"),
    HEAVY("무거운 제품"),
    FLASHY_COLOR("화려한 색상"),
    SMALL_STORAGE("작은 수납공간"),
    HARD_TO_CARE("관리가 어려운 소재"),
    NONE("특별히 없음");

    private final String label;
}
