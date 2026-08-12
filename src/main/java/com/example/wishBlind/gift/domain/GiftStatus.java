package com.example.wishBlind.gift.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 선물 세션 상태. 홈 대시보드 카드의 배지와 1:1 대응.
 */
@Getter
@RequiredArgsConstructor
public enum GiftStatus {

    CREATED("생성됨"),
    INVITED("취향 입력 대기"),
    ANALYZING("AI 분석 중"),
    RECOMMENDED("AI 추천 완료"),
    FINALIZED("선물 선택 완료"),
    PREPARING("배송 준비 중"),
    COMPLETED("선물 완료");

    private final String label;
}
