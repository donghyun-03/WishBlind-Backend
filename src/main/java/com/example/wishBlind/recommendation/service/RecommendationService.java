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
import com.example.wishBlind.product.repository.ProductRepository;
import com.example.wishBlind.recipient.domain.RecipientPreference;
import com.example.wishBlind.recipient.repository.RecipientPreferenceRepository;
import com.example.wishBlind.recommendation.domain.Recommendation;
import com.example.wishBlind.recommendation.domain.TasteAnalysis;
import com.example.wishBlind.recommendation.dto.RecommendationDetailResponse;
import com.example.wishBlind.recommendation.dto.RecommendationResponse;
import com.example.wishBlind.recommendation.repository.RecommendationRepository;
import com.example.wishBlind.notification.domain.NotificationType;
import com.example.wishBlind.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
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
    private final ProductRepository productRepository;
    private final AiClient aiClient;
    private final NotificationService notificationService;
    private final PlatformTransactionManager txManager;

    /**
     * 두 사람 정보 결합 → 후보 3개 생성. 상태 RECOMMENDED 전환.
     *
     * 느린 외부 AI 호출(후보 3개 × Gemini)을 DB 트랜잭션 밖에서 수행한다.
     * 트랜잭션 안에서 호출하면 커넥션을 수십 초 쥔 채 대기하다가 DB가 커넥션을 끊어
     * 간헐적으로 500(Communications link failure)이 났다. 그래서 단계를 분리한다:
     *   1) 읽기 트랜잭션: 점수화 + 상위 3개 확정(지연 로딩 값까지 모두 확정)
     *   2) 트랜잭션 밖: Gemini 호출로 코멘트 생성(커넥션을 쥐지 않음)
     *   3) 짧은 쓰기 트랜잭션: 저장 + 상태 전환 + 알림
     * 이 메서드 자체는 트랜잭션을 열지 않는다(NOT_SUPPORTED). 각 단계가 자기 트랜잭션을 쓴다.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<RecommendationResponse> generate(Long giftSessionId, Long userId) {
        TransactionTemplate readTx = new TransactionTemplate(txManager);
        readTx.setReadOnly(true);
        TransactionTemplate writeTx = new TransactionTemplate(txManager);

        // 1) 읽기 트랜잭션: 후보 점수화 + 상위 3개 확정
        GenerationPlan plan = readTx.execute(s -> preparePlan(giftSessionId, userId));

        // 2) 트랜잭션 밖: 느린 Gemini 호출(코멘트 생성). 실패 시 규칙 기반으로 자동 폴백.
        List<PreparedRec> prepared = new ArrayList<>();
        int rank = 1;
        for (ScoredPlain sp : plan.scored()) {
            AiRecommendationResult ai = aiClient.generateComment(new AiRecommendationCommand(
                    plan.occasion(),
                    plan.meaning(),
                    plan.moodLabels(),
                    sp.productName(),
                    sp.matchRate(),
                    sp.reasons(),
                    sp.considerations()
            ));
            prepared.add(new PreparedRec(sp.productId(), rank, rank == 1, sp.matchRate(),
                    sp.tags(), sp.reasons(), sp.considerations(), sp.tasteAnalysis(), ai.comment()));
            rank++;
        }

        // 3) 짧은 쓰기 트랜잭션: 저장 + 상태 전환 + 알림
        writeTx.executeWithoutResult(s -> persistPlan(giftSessionId, userId, prepared));

        // 4) 응답은 읽기 트랜잭션에서(지연 컬렉션 직렬화 안전)
        return readTx.execute(s -> toResponses(giftSessionId));
    }

    /** 1단계: 세션·취향 로드 → 후보 점수화 → 상위 3개. 지연 로딩 값을 모두 평범한 값으로 확정한다. */
    private GenerationPlan preparePlan(Long giftSessionId, Long userId) {
        GiftSession session = giftSessionService.findOwned(giftSessionId, userId);
        RecipientPreference pref = preferenceRepository.findByGiftSession_Id(giftSessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PREFERENCE_NOT_SUBMITTED));

        List<Product> candidates = productFilterService.filter(session);
        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_CANDIDATE);
        }

        List<ScoredPlain> scored = candidates.stream()
                .map(p -> {
                    MatchResult r = matchScoreService.score(pref, p);
                    return new ScoredPlain(
                            p.getId(), p.getName(), r.matchRate(),
                            new ArrayList<>(r.tags()), new ArrayList<>(r.reasons()),
                            new ArrayList<>(r.considerations()), r.tasteAnalysis());
                })
                .sorted(Comparator.comparingInt(ScoredPlain::matchRate).reversed())
                .limit(TOP_N)
                .toList();

        List<String> moodLabels = (session.getMoods() == null) ? List.of()
                : session.getMoods().stream().map(GiftMood::getLabel).toList();

        return new GenerationPlan(session.getOccasion(), session.getMeaning(), moodLabels, scored);
    }

    /** 3단계: 기존 추천 초기화 → 신규 저장 → 상태 전환 → 알림. */
    private void persistPlan(Long giftSessionId, Long userId, List<PreparedRec> prepared) {
        GiftSession session = giftSessionService.findById(giftSessionId);
        recommendationRepository.deleteByGiftSession_Id(giftSessionId);

        for (PreparedRec pr : prepared) {
            Product product = productRepository.getReferenceById(pr.productId());
            Recommendation rec = Recommendation.builder()
                    .giftSession(session)
                    .product(product)
                    .rank(pr.rank())
                    .best(pr.best())
                    .matchRate(pr.matchRate())
                    .tags(pr.tags())
                    .reasons(pr.reasons())
                    .considerations(pr.considerations())
                    .tasteAnalysis(pr.tasteAnalysis())
                    .aiComment(pr.aiComment())
                    .build();
            recommendationRepository.save(rec);
        }

        session.changeStatus(GiftStatus.RECOMMENDED);
        notificationService.notify(userId, NotificationType.RECOMMENDED,
                "AI 추천 후보가 준비됐어요. 확인해보세요.", giftSessionId);
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

    /** 1단계 산출물: 세션 요약 + 상위 후보(값으로 확정된 것). */
    private record GenerationPlan(String occasion, String meaning, List<String> moodLabels,
                                  List<ScoredPlain> scored) {
    }

    /** 점수화된 후보(지연 로딩 없이 평범한 값만). */
    private record ScoredPlain(Long productId, String productName, int matchRate,
                               List<String> tags, List<String> reasons, List<String> considerations,
                               TasteAnalysis tasteAnalysis) {
    }

    /** 저장 직전 준비물(AI 코멘트까지 포함). */
    private record PreparedRec(Long productId, int rank, boolean best, int matchRate,
                               List<String> tags, List<String> reasons, List<String> considerations,
                               TasteAnalysis tasteAnalysis, String aiComment) {
    }
}
