package com.example.wishBlind.ai;

import java.util.List;
import java.util.Map;

/**
 * AI 추천 요청. 5번 추천 모듈이 채워서 넘긴다.
 *
 * 이 record가 두 모듈 사이의 계약 전부다 — 팀원 엔티티가 바뀌어도 여기만 그대로면
 * AI 쪽 코드는 안 깨진다.
 *
 * @param relation             관계 (친구/연인/가족 등)
 * @param anniversaryType      기념일 종류
 * @param budgetMin            예산 하한(원)
 * @param budgetMax            예산 상한(원) — 후처리 검증의 하드 기준
 * @param meaningText          선물에 담고 싶은 의미 (자유 입력)
 * @param moods                선물 분위기 (최대 2개)
 * @param recipientPreferences 수령자 취향 5스텝 응답 (문항코드 → 선택지들)
 * @param avoidElements        수령자가 피하고 싶은 요소
 * @param candidates           1차 축소된 후보 상품
 */
public record AiRecommendCommand(
        String relation,
        String anniversaryType,
        int budgetMin,
        int budgetMax,
        String meaningText,
        List<String> moods,
        Map<String, List<String>> recipientPreferences,
        List<String> avoidElements,
        List<CandidateProduct> candidates
) {
}
