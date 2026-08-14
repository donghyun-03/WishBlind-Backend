package com.example.wishBlind.recommendation.dto;

import com.example.wishBlind.product.domain.Product;
import com.example.wishBlind.recommendation.domain.Recommendation;
import com.example.wishBlind.recommendation.domain.TasteAnalysis;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "AI 추천 상세 응답 (근거·별점·코멘트·고려할 점)")
public record RecommendationDetailResponse(
        Long recommendationId,
        int rank,
        boolean best,
        int matchRate,
        List<String> tags,
        Long productId,
        String productName,
        String brand,
        Integer price,
        String imageUrl,
        List<String> reasons,
        List<String> considerations,
        TasteAnalysisDto tasteAnalysis,
        String aiComment,
        boolean chosen
) {

    @Schema(description = "취향 분석 별점(1~5)")
    public record TasteAnalysisDto(int colorStars, int styleStars, int practicalityStars) {
        static TasteAnalysisDto from(TasteAnalysis t) {
            if (t == null) {
                return new TasteAnalysisDto(0, 0, 0);
            }
            return new TasteAnalysisDto(t.getColorStars(), t.getStyleStars(), t.getPracticalityStars());
        }
    }

    public static RecommendationDetailResponse from(Recommendation r) {
        Product p = r.getProduct();
        return new RecommendationDetailResponse(
                r.getId(),
                r.getRank(),
                r.isBest(),
                r.getMatchRate(),
                new ArrayList<>(r.getTags()),
                p.getId(),
                p.getName(),
                p.getBrand(),
                p.getPrice(),
                p.getImageUrl(),
                new ArrayList<>(r.getReasons()),
                new ArrayList<>(r.getConsiderations()),
                TasteAnalysisDto.from(r.getTasteAnalysis()),
                r.getAiComment(),
                r.isChosen()
        );
    }
}
