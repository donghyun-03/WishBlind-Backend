package com.example.wishBlind.ai.dto;

import java.util.List;

/**
 * AI에게 넘길 후보 하나에 대한 추천 코멘트 생성 입력.
 */
public record AiRecommendationCommand(
        String occasion,          // 기념일/목적
        String meaning,           // 전하고 싶은 의미
        List<String> moods,       // 선물 분위기(라벨)
        String productName,       // 후보 상품명
        int matchRate,            // 취향 일치율
        List<String> reasons,     // 매칭 근거
        List<String> considerations // 고려할 점
) {
}
