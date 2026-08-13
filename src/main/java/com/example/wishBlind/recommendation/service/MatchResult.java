package com.example.wishBlind.recommendation.service;

import com.example.wishBlind.recommendation.domain.TasteAnalysis;

import java.util.List;

/**
 * 취향-상품 매칭 계산 결과(엔티티 저장 전 중간 산출물).
 */
public record MatchResult(
        int matchRate,
        TasteAnalysis tasteAnalysis,
        List<String> reasons,
        List<String> considerations,
        List<String> tags
) {
}
