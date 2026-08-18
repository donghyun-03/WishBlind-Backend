package com.example.wishBlind.storefitting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "매장 체험 예약 생성 요청")
public record FittingCreateRequest(
        @Schema(description = "선물 세션 ID") @NotNull Long giftSessionId,
        @Schema(description = "고객명", example = "김사자") String customerName,
        @Schema(description = "예약 날짜", example = "2026-08-20") @NotNull LocalDate reserveDate,
        @Schema(description = "예약 시간(시작)", example = "14:00") @NotNull LocalTime reserveTime
) {
}
