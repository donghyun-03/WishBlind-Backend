package com.example.wishBlind.recipient.dto;

import com.example.wishBlind.recipient.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "블라인드 취향 제출 요청 (STEP 01~05)")
public record PreferenceSubmitRequest(

        @Schema(description = "선호 색상(복수)")
        @NotEmpty(message = "색상을 하나 이상 선택해주세요.")
        List<PreferColor> colors,

        @Schema(description = "선호 분위기")
        @NotNull PreferMood mood,

        @Schema(description = "선호 소재")
        @NotNull PreferMaterial material,

        @Schema(description = "로고 노출 정도")
        @NotNull LogoVisibility logoVisibility,

        @Schema(description = "원하는 크기")
        @NotNull PreferSize size,

        @Schema(description = "착용 방식(카테고리에 따라 선택). 옷 카테고리는 비워도 됨")
        WearStyle wearStyle,

        @Schema(description = "피하고 싶은 요소(복수)")
        List<AvoidFactor> avoid,

        @Schema(description = "추가로 피하고 싶은 것(자유 입력)", example = "너무 밝은 색")
        @Size(max = 500) String avoidEtc
) {
}
