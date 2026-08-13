package com.example.wishBlind.recommendation.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 취향 분석 별점(1~5). AI 추천 상세 화면의 "취향 분석"(색상/스타일/실용성) 대응.
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TasteAnalysis {

    private int colorStars;
    private int styleStars;
    private int practicalityStars;

    @Builder
    public TasteAnalysis(int colorStars, int styleStars, int practicalityStars) {
        this.colorStars = colorStars;
        this.styleStars = styleStars;
        this.practicalityStars = practicalityStars;
    }
}
