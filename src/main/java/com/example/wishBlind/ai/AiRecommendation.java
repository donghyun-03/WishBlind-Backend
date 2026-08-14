package com.example.wishBlind.ai;

import java.util.List;
import java.util.Map;

/**
 * AI 추천 결과. 항상 rank 1~3의 pick 3개.
 *
 * @param picks rank 오름차순. rank 1이 구성도의 "BEST 후보 A"
 */
public record AiRecommendation(List<Pick> picks) {

    /**
     * @param productId      선택된 상품 ID (반드시 입력 후보 안에 있는 값)
     * @param rank           1~3
     * @param matchScore     취향 일치율 0~100
     * @param reasonSummary  추천 핵심 이유 한 줄
     * @param reasons        추천 이유 (의미 적합 / 색상 반영 / 스타일 적합 / 예산 만족 / 브랜드 적합)
     * @param scoreBreakdown 취향 분석 점수 (color / style / practicality → 0~100)
     * @param aiComment      AI 코멘트
     * @param considerations 고려할 점 (사이즈, 착용·사용 주의 등)
     */
    public record Pick(
            Long productId,
            int rank,
            int matchScore,
            String reasonSummary,
            List<String> reasons,
            Map<String, Integer> scoreBreakdown,
            String aiComment,
            List<String> considerations
    ) {
    }
}
