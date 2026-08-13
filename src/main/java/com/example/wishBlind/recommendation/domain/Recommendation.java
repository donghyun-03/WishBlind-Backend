package com.example.wishBlind.recommendation.domain;

import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.global.common.BaseEntity;
import com.example.wishBlind.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 추천 후보 하나 (GiftSession 1:N, 보통 3개).
 * 규칙 매칭 결과(matchRate/별점/근거/고려할 점) + AI 코멘트를 담는다.
 */
@Getter
@Entity
@Table(name = "recommendation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recommendation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_session_id", nullable = false)
    private GiftSession giftSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "ranking")  // 'rank'는 MySQL 8 예약어라 컬럼명 변경
    private int rank;          // 1~3
    private boolean best;      // BEST 표시(rank 1)
    private int matchRate;     // 취향 일치율 %

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recommendation_tag",
            joinColumns = @JoinColumn(name = "recommendation_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recommendation_reason",
            joinColumns = @JoinColumn(name = "recommendation_id"))
    @Column(name = "reason", length = 300)
    private List<String> reasons = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recommendation_consideration",
            joinColumns = @JoinColumn(name = "recommendation_id"))
    @Column(name = "consideration", length = 300)
    private List<String> considerations = new ArrayList<>();

    @Embedded
    private TasteAnalysis tasteAnalysis;

    @Column(columnDefinition = "TEXT")
    private String aiComment;

    private boolean chosen;    // 선물자가 최종 선택한 후보(isFinal)

    @Builder
    public Recommendation(GiftSession giftSession, Product product, int rank, boolean best,
                          int matchRate, List<String> tags, List<String> reasons,
                          List<String> considerations, TasteAnalysis tasteAnalysis, String aiComment) {
        this.giftSession = giftSession;
        this.product = product;
        this.rank = rank;
        this.best = best;
        this.matchRate = matchRate;
        if (tags != null) this.tags = tags;
        if (reasons != null) this.reasons = reasons;
        if (considerations != null) this.considerations = considerations;
        this.tasteAnalysis = tasteAnalysis;
        this.aiComment = aiComment;
    }

    public void updateChosen(boolean chosen) {
        this.chosen = chosen;
    }
}
