package com.example.wishBlind.gift.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 선물 분위기(선물자 STEP 02). 최대 2개 선택.
 */
@Getter
@RequiredArgsConstructor
public enum GiftMood {

    PRACTICAL("실용적"),
    SPECIAL("특별한"),
    COMMEMORATIVE("기념용"),
    LUXURY("럭셔리"),
    TOUCHING("감동적인"),
    ETC("기타");

    private final String label;
}
