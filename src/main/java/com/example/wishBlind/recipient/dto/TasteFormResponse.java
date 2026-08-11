package com.example.wishBlind.recipient.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 취향 테스트 문항. 카테고리에 따라 동적으로 구성된다.
 * (예: 가방·목걸이는 착용 방식 문항 포함, 옷은 제외)
 */
@Schema(description = "취향 테스트 폼 (카테고리별 동적 구성)")
public record TasteFormResponse(
        @Schema(description = "카테고리", example = "가방") String category,
        @Schema(description = "문항 목록(순서대로)") List<Step> steps
) {

    @Schema(description = "문항 하나")
    public record Step(
            @Schema(description = "필드 키", example = "colors") String key,
            @Schema(description = "질문 제목", example = "선호하는 색상을 골라주세요") String title,
            @Schema(description = "선택 방식", example = "MULTI", allowableValues = {"SINGLE", "MULTI"}) String selectType,
            @Schema(description = "자유 입력 허용 여부") boolean allowEtc,
            @Schema(description = "선택지") List<Option> options
    ) {
    }

    @Schema(description = "선택지")
    public record Option(
            @Schema(description = "코드(제출 시 사용)", example = "BLACK") String code,
            @Schema(description = "화면 표기", example = "블랙") String label
    ) {
    }
}
