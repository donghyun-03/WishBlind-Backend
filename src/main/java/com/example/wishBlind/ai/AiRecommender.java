package com.example.wishBlind.ai;

/**
 * 선물자 의도 + 수령자 취향 + 후보 상품을 받아 최종 추천 3개를 고른다.
 *
 * 경계: 이 포트는 DB도 도메인도 모른다. 후보를 어떻게 축소했는지, 결과를 어디에 저장하는지는
 * 호출자(5번 추천 모듈)의 책임이다. 반대로 호출자는 LLM을 몰라도 된다.
 */
public interface AiRecommender {

    /**
     * @throws com.example.wishBlind.global.exception.BusinessException
     *         LLM_NO_CANDIDATE / LLM_TIMEOUT / LLM_PARSE_ERROR / LLM_REFUSAL / LLM_INVALID_PICK
     */
    AiRecommendation recommend(AiRecommendCommand command);
}
