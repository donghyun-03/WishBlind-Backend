package com.example.wishBlind.ai;

import com.example.wishBlind.ai.dto.AiRecommendationCommand;
import com.example.wishBlind.ai.dto.AiRecommendationResult;
import org.springframework.stereotype.Component;

/**
 * LLM API 키 없이도 동작하는 규칙 기반 코멘트 생성기(폴백).
 * 실제 LLM 연동 전까지 이 구현이 사용된다.
 */
@Component
public class RuleBasedAiClient implements AiClient {

    @Override
    public AiRecommendationResult generateComment(AiRecommendationCommand c) {
        StringBuilder sb = new StringBuilder();

        if (c.occasion() != null && !c.occasion().isBlank()) {
            sb.append("‘").append(c.occasion()).append("’ 의미를 고려하면, ");
        }
        sb.append(c.productName()).append("이(가) 취향 일치율 ").append(c.matchRate()).append("%로 잘 맞습니다. ");

        if (c.reasons() != null && !c.reasons().isEmpty()) {
            sb.append(c.reasons().get(0)).append(".");
        }
        if (c.considerations() != null && !c.considerations().isEmpty()) {
            sb.append(" 다만 ").append(c.considerations().get(0)).append(".");
        }

        return new AiRecommendationResult(sb.toString().trim());
    }
}
