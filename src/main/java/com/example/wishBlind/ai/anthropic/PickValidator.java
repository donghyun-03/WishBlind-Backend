package com.example.wishBlind.ai.anthropic;

import com.example.wishBlind.ai.AiRecommendCommand;
import com.example.wishBlind.ai.AiRecommendation;
import com.example.wishBlind.ai.CandidateProduct;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * LLM이 돌려준 추천을 코드로 다시 검사한다.
 *
 * 왜 필요한가: 예산 초과와 존재하지 않는 상품은 프롬프트로 아무리 금지해도 샌다.
 * 결정적으로 막을 수 있는 조건은 결정적으로 막는다.
 *
 * 여기서 폴백(룰 스코어 상위로 채우기)을 하지 않는 이유: 후보를 어떻게 고를지는
 * 5번 추천 모듈의 책임이다. AI 연결은 "못 만들었다"까지만 알린다.
 */
public final class PickValidator {

    /** 구성도 7번이 요구하는 후보 개수. */
    public static final int REQUIRED_PICKS = 3;

    private PickValidator() {
    }

    /**
     * @return rank 오름차순으로 정렬된 유효한 pick 3개
     * @throws BusinessException 유효한 pick이 3개를 못 채우면 LLM_INVALID_PICK
     */
    public static List<AiRecommendation.Pick> validate(List<AiRecommendation.Pick> raw,
                                                       AiRecommendCommand command) {
        Map<Long, CandidateProduct> candidatesById = command.candidates().stream()
                .collect(Collectors.toMap(
                        CandidateProduct::productId, Function.identity(), (first, dup) -> first));

        Set<Long> seenProductIds = new HashSet<>();
        Set<Integer> seenRanks = new HashSet<>();
        List<AiRecommendation.Pick> kept = new ArrayList<>();

        for (AiRecommendation.Pick pick : raw) {
            CandidateProduct candidate = candidatesById.get(pick.productId());
            if (candidate == null) {
                continue; // 후보에 없는 상품 = 환각
            }
            if (candidate.price() > command.budgetMax()) {
                continue; // 예산 초과
            }
            if (pick.matchScore() < 0 || pick.matchScore() > 100) {
                continue;
            }
            if (pick.rank() < 1 || pick.rank() > REQUIRED_PICKS) {
                continue;
            }
            if (!seenProductIds.add(pick.productId()) || !seenRanks.add(pick.rank())) {
                continue; // 같은 상품 또는 같은 순위 중복
            }
            kept.add(pick);
        }

        if (kept.size() < REQUIRED_PICKS) {
            throw new BusinessException(ErrorCode.LLM_INVALID_PICK);
        }
        return kept.stream()
                .sorted(Comparator.comparingInt(AiRecommendation.Pick::rank))
                .toList();
    }
}
