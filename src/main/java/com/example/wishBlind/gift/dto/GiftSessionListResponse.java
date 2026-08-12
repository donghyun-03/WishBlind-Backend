package com.example.wishBlind.gift.dto;

import com.example.wishBlind.gift.domain.GiftSession;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "홈 대시보드 카드용 요약 응답")
public record GiftSessionListResponse(
        @Schema(description = "선물 세션 ID") Long id,
        @Schema(description = "기념일/목적", example = "취업 축하") String occasion,
        @Schema(description = "관계(받는 사람)", example = "여자친구") String relationship,
        @Schema(description = "상태 코드") String status,
        @Schema(description = "상태 라벨(배지 표기)", example = "AI 추천 완료") String statusLabel,
        LocalDateTime createdAt
) {

    public static GiftSessionListResponse from(GiftSession s) {
        return new GiftSessionListResponse(
                s.getId(),
                s.getOccasion(),
                s.getRelationship(),
                s.getStatus().name(),
                s.getStatus().getLabel(),
                s.getCreatedAt()
        );
    }
}
