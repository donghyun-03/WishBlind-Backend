package com.example.wishBlind.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 선물 공개 응답 (받는 사람).
 * 상태가 COMPLETED(선물 완료)가 되기 전까지는 revealed=false 로 상품을 숨긴다.
 */
@Schema(description = "선물 공개 응답")
public record RevealResponse(
        @Schema(description = "공개 여부") boolean revealed,
        @Schema(description = "안내 문구") String notice,
        @Schema(description = "브랜드 (공개 시)") String brand,
        @Schema(description = "최종 상품명 (공개 시)") String productName,
        @Schema(description = "가격 (공개 시)") Integer price,
        @Schema(description = "이미지 (공개 시)") String imageUrl,
        @Schema(description = "선물하는 사람의 메시지 (공개 시)") String message
) {

    public static RevealResponse hidden() {
        return new RevealResponse(false,
                "최종 상품은 선물을 받는 순간에 공개돼요. 조금만 기다려주세요!",
                null, null, null, null, null);
    }

    public static RevealResponse revealed(String brand, String productName, Integer price,
                                          String imageUrl, String message) {
        return new RevealResponse(true, "선물이 공개되었습니다!", brand, productName, price, imageUrl, message);
    }
}
