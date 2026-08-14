package com.example.wishBlind.ai.anthropic;

import java.util.List;

/**
 * LLM이 채워서 돌려주는 구조화 출력 스키마.
 *
 * 점수를 Map이 아니라 평평한 int 3개로 둔 이유: structured output의 JSON Schema는
 * additionalProperties=false를 요구해서 임의 키를 갖는 Map을 표현할 수 없다.
 */
public record AiRecommendationPayload(List<PickPayload> picks) {

    public record PickPayload(
            long productId,
            int rank,
            int matchScore,
            String reasonSummary,
            List<String> reasons,
            int colorScore,
            int styleScore,
            int practicalityScore,
            String aiComment,
            List<String> considerations
    ) {
    }
}
