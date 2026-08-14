package com.example.wishBlind.ai.anthropic;

import com.example.wishBlind.ai.AiRecommendCommand;
import com.example.wishBlind.ai.AiRecommendation;
import com.example.wishBlind.ai.CandidateProduct;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * LLM 출력 후처리 검증.
 * 이 테스트가 통과해야 "예산 초과 상품이 추천되지 않는다"를 주장할 수 있다.
 */
class PickValidatorTest {

    private static final int BUDGET_MAX = 100_000;

    @Test
    @DisplayName("후보 안에 있고 예산을 지킨 3개는 rank 순으로 통과한다")
    void keepsValidPicksSortedByRank() {
        AiRecommendCommand command = command(
                candidate(1L, 50_000), candidate(2L, 60_000), candidate(3L, 70_000));

        List<AiRecommendation.Pick> result = PickValidator.validate(
                List.of(pick(3L, 3), pick(1L, 1), pick(2L, 2)), command);

        assertThat(result).extracting(AiRecommendation.Pick::rank).containsExactly(1, 2, 3);
        assertThat(result).extracting(AiRecommendation.Pick::productId).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("후보에 없는 상품(환각)은 버린다 — 3개를 못 채우면 LLM_INVALID_PICK")
    void dropsHallucinatedProduct() {
        AiRecommendCommand command = command(
                candidate(1L, 50_000), candidate(2L, 60_000), candidate(3L, 70_000));

        assertThatThrownBy(() -> PickValidator.validate(
                List.of(pick(1L, 1), pick(2L, 2), pick(999L, 3)), command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.LLM_INVALID_PICK);
    }

    @Test
    @DisplayName("예산 상한을 넘는 상품은 버린다")
    void dropsOverBudgetProduct() {
        AiRecommendCommand command = command(
                candidate(1L, 50_000), candidate(2L, 60_000), candidate(3L, BUDGET_MAX + 1));

        assertThatThrownBy(() -> PickValidator.validate(
                List.of(pick(1L, 1), pick(2L, 2), pick(3L, 3)), command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.LLM_INVALID_PICK);
    }

    @Test
    @DisplayName("무효한 항목이 섞여 있어도 유효한 3개가 남으면 통과한다")
    void keepsThreeWhenExtraInvalidPickPresent() {
        AiRecommendCommand command = command(
                candidate(1L, 50_000), candidate(2L, 60_000), candidate(3L, 70_000));

        List<AiRecommendation.Pick> result = PickValidator.validate(
                List.of(pick(999L, 1), pick(1L, 1), pick(2L, 2), pick(3L, 3)), command);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(AiRecommendation.Pick::productId).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("같은 순위가 중복되면 뒤엣것을 버린다")
    void dropsDuplicateRank() {
        AiRecommendCommand command = command(
                candidate(1L, 50_000), candidate(2L, 60_000), candidate(3L, 70_000));

        assertThatThrownBy(() -> PickValidator.validate(
                List.of(pick(1L, 1), pick(2L, 1), pick(3L, 2)), command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.LLM_INVALID_PICK);
    }

    @Test
    @DisplayName("matchScore가 0~100 밖이면 버린다")
    void dropsOutOfRangeMatchScore() {
        AiRecommendCommand command = command(
                candidate(1L, 50_000), candidate(2L, 60_000), candidate(3L, 70_000));

        List<AiRecommendation.Pick> raw = List.of(
                pick(1L, 1), pick(2L, 2), pickWithScore(3L, 3, 120));

        assertThatThrownBy(() -> PickValidator.validate(raw, command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.LLM_INVALID_PICK);
    }

    private AiRecommendCommand command(CandidateProduct... candidates) {
        return new AiRecommendCommand(
                "친구", "생일", 30_000, BUDGET_MAX, "고마운 마음",
                List.of("실용적"), Map.of("COLOR", List.of("블랙")), List.of("큰 로고"),
                List.of(candidates));
    }

    private CandidateProduct candidate(Long id, int price) {
        return new CandidateProduct(id, "브랜드", "상품" + id, "가방", price, Map.of("color", "black"));
    }

    private AiRecommendation.Pick pick(Long productId, int rank) {
        return pickWithScore(productId, rank, 80);
    }

    private AiRecommendation.Pick pickWithScore(Long productId, int rank, int matchScore) {
        return new AiRecommendation.Pick(
                productId, rank, matchScore, "이유 요약", List.of("예산 범위 만족"),
                Map.of("color", 80, "style", 70, "practicality", 90),
                "코멘트", List.of());
    }
}
