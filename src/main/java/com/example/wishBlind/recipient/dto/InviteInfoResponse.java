package com.example.wishBlind.recipient.dto;

import com.example.wishBlind.gift.domain.GiftSession;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 받는 사람에게 보여줄 초대 정보.
 * 블라인드 보장: 브랜드·카테고리만 노출하고 제품명·가격·후보·최종 상품은 절대 포함하지 않는다.
 */
@Schema(description = "초대 확인 응답 (블라인드 — 브랜드/카테고리만)")
public record InviteInfoResponse(
        @Schema(description = "선물 세션 ID") Long giftSessionId,
        @Schema(description = "브랜드", example = "MCM") String brand,
        @Schema(description = "카테고리", example = "가방") String category,
        @Schema(description = "안내 문구") String notice
) {

    private static final String NOTICE = "최종 상품은 선물을 받는 순간까지 공개되지 않아요. 취향만 편하게 골라주세요.";

    public static InviteInfoResponse from(GiftSession s) {
        return new InviteInfoResponse(s.getId(), s.getBrand(), s.getCategory(), NOTICE);
    }
}
