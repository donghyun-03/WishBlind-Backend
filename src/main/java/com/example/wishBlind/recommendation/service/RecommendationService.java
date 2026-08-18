package com.example.wishBlind.recommendation.service;

import com.example.wishBlind.ai.AiClient;
import com.example.wishBlind.ai.dto.AiRecommendationCommand;
import com.example.wishBlind.ai.dto.AiRecommendationResult;
import com.example.wishBlind.gift.domain.GiftMood;
import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.gift.domain.GiftStatus;
import com.example.wishBlind.gift.service.GiftSessionService;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import com.example.wishBlind.product.domain.Product;
import com.example.wishBlind.recipient.domain.RecipientPreference;
import com.example.wishBlind.recipient.repository.RecipientPreferenceRepository;
import com.example.wishBlind.recommendation.domain.Recommendation;
import com.example.wishBlind.recommendation.dto.RecommendationDetailResponse;
import com.example.wishBlind.recommendation.dto.RecommendationResponse;
import com.example.wishBlind.recommendation.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int TOP_N = 3;

    private final GiftSessionService giftSessionService;
    private final RecipientPreferenceRepository preferenceRepository;
    private final ProductFilterService productFilterService;
    private final MatchScoreService matchScoreService;
    private final RecommendationRepository recommendationRepository;
    private final AiClient aiClient;

    /** 두 사람 정보 결합 → 후보 3개 생성. 상태 RECOMMENDED 전환. */
    @Transactional
    public List<RecommendationResponse> generate(Long giftSessionId, Long userId) {
        GiftSession session = giftSessionService.findOwned(giftSessionId, userId);
        RecipientPreference pref = preferenceRepository.findByGiftSession_Id(giftSessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PREFERENCE_NOT_SUBMITTED));

        List<Product> candidates = productFilterService.filter(session);
        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_CANDIDATE);
        }

        List<Scored> top = candidates.stream()
                .map(p -> new Scored(p, matchScoreService.score(pref, p)))
                .sorted(Comparator.comparingInt((Scored s) -> s.result().matchRate()).reversed())
                .limit(TOP_N)
                .toList();

        // 재실행 시 기존 추천 초기화
        recommendationRepository.deleteByGiftSession_Id(giftSessionId);

        List<GiftMood> moods = session.getMoods();
        int rank = 1;
        for (Scored s : top) {
            MatchResult r = s.result();
            AiRecommendationResult ai = aiClient.generateComment(new AiRecommendationCommand(
                    session.getOccasion(),
                    session.getMeaning(),
                    moods == null ? List.of() : moods.stream().map(GiftMood::getLabel).toList(),
                    s.product().getName(),
                    r.matchRate(),
                    r.reasons(),
                    r.considerations()
            ));

            Recommendation rec = Recommendation.builder()
                    .giftSession(session)
                    .product(s.product())
                    .rank(rank)
                    .best(rank == 1)
                    .matchRate(r.matchRate())
                    .tags(r.tags())
                    .reasons(r.reasons())
                    .considerations(r.considerations())
                    .tasteAnalysis(r.tasteAnalysis())
                    .aiComment(ai.comment())
                    .build();
            recommendationRepository.save(rec);
            rank++;
        }

        session.changeStatus(GiftStatus.RECOMMENDED);
        // 소유자 확인은 이 메서드 진입 시 이미 끝났다.
        return toResponses(giftSessionId);
    }

    public List<RecommendationResponse> getList(Long giftSessionId, Long userId) {
        giftSessionService.findOwned(giftSessionId, userId);
        return toResponses(giftSessionId);
    }

    private List<RecommendationResponse> toResponses(Long giftSessionId) {
        return recommendationRepository.findByGiftSession_IdOrderByRankAsc(giftSessionId).stream()
                .map(RecommendationResponse::from)
                .toList();
    }

    public RecommendationDetailResponse getDetail(Long recommendationId, Long userId) {
        Recommendation recommendation = findById(recommendationId);
        if (!recommendation.getGiftSession().isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.GIFT_SESSION_FORBIDDEN);
        }
        return RecommendationDetailResponse.from(recommendation);
    }

    /** 선물자가 최종 상품 선택 → 상태 FINALIZED. */
    @Transactional
    public void finalizeSelection(Long giftSessionId, Long recommendationId, Long userId) {
        GiftSession session = giftSessionService.findOwned(giftSessionId, userId);
        List<Recommendation> recs = recommendationRepository.findByGiftSession_IdOrderByRankAsc(giftSessionId);

        boolean found = false;
        for (Recommendation rec : recs) {
            boolean selected = rec.getId().equals(recommendationId);
            rec.updateChosen(selected);
            if (selected) {
                found = true;
            }
        }
        if (!found) {
            throw new BusinessException(ErrorCode.RECOMMENDATION_NOT_FOUND);
        }
        session.changeStatus(GiftStatus.FINALIZED);
    }

    private Recommendation findById(Long id) {
        return recommendationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECOMMENDATION_NOT_FOUND));
    }

    /** 내부 정렬용 (상품 + 매칭 결과). */
    private record Scored(Product product, MatchResult result) {
    }
}
