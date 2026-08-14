package com.example.wishBlind.ai;

import com.example.wishBlind.ai.dto.AiRecommendationCommand;
import com.example.wishBlind.ai.dto.AiRecommendationResult;

/**
 * LLM 추천 코멘트 생성 클라이언트.
 * 규칙 필터·점수는 코드가 담당하고, 이 클라이언트는 "의미 해석 + 추천 코멘트"만 맡는다.
 * 기본 구현은 규칙 기반 폴백({@link RuleBasedAiClient})이며,
 * 나중에 OpenAI/Claude 구현을 @Primary로 추가하면 자동으로 교체된다.
 */
public interface AiClient {

    AiRecommendationResult generateComment(AiRecommendationCommand command);
}
