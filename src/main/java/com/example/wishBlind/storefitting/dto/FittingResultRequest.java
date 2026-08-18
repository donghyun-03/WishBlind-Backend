package com.example.wishBlind.storefitting.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "체험 결과 저장 요청 (진행 4스텝 결과)")
public record FittingResultRequest(
        @Schema(description = "고객이 가장 선호한 후보 상품 ID") Long preferredCandidateProductId,
        @Schema(description = "소재 체험 결과", example = "부드러운 가죽 선호") String materialFeel,
        @Schema(description = "크기", example = "Small 선호") String sizeFeel,
        @Schema(description = "수납감", example = "보통") String storageFeel,
        @Schema(description = "착용감", example = "불편함 호소") String wearComfort,
        @Schema(description = "무게", example = "가벼운 제품 선호") String weight,
        @Schema(description = "전체 만족도", example = "만족") String overallSatisfaction,
        @Schema(description = "직원 메모", example = "스트랩이 긴 제품에 긍정적인 반응을 보임.") String staffMemo
) {
}
