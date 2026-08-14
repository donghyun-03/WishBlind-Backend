package com.example.wishBlind.ai;

import java.util.Map;

/**
 * 추천 후보 상품 한 건. 5번 추천 모듈이 카탈로그에서 1차 축소해 넘겨준다.
 *
 * @param productId  상품 ID
 * @param brand      브랜드명
 * @param name       상품명
 * @param category   카테고리
 * @param price      가격(원)
 * @param attributes 속성 (color/material/logo_exposure/size/style 등)
 */
public record CandidateProduct(
        Long productId,
        String brand,
        String name,
        String category,
        int price,
        Map<String, String> attributes
) {
}
