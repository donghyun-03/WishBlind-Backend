package com.example.wishBlind.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "최종 선택 요청")
public record FinalizeRequest(
        @Schema(description = "선택한 추천 ID")
        @NotNull Long recommendationId
) {
}
