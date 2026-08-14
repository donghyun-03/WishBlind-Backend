package com.example.wishBlind.recommendation.dto;

import com.example.wishBlind.product.domain.Product;
import com.example.wishBlind.recommendation.domain.Recommendation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "AI 추천 후보 목록 응답 (선물자용)")
public record RecommendationResponse(
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
        boolean chosen
) {

    public static RecommendationResponse from(Recommendation r) {
        Product p = r.getProduct();
        return new RecommendationResponse(
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
                r.isChosen()
        );
    }
}
