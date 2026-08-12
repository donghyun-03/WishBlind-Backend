package com.example.wishBlind.gift.dto;

import com.example.wishBlind.gift.domain.GiftMood;
import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.gift.domain.GiverKnownTaste;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;

@Schema(description = "선물 세션 생성 요청 (선물자 입력 STEP 01~03)")
public record GiftSessionCreateRequest(

        @Schema(description = "관계", example = "여자친구")
        @NotBlank String relationship,

        @Schema(description = "기념일/목적", example = "취업 축하")
        @NotBlank String occasion,

        @Schema(description = "예산 하한(원)", example = "300000")
        @NotNull @PositiveOrZero Integer budgetMin,

        @Schema(description = "예산 상한(원)", example = "800000")
        @NotNull @Positive Integer budgetMax,

        @Schema(description = "카테고리", example = "가방")
        @NotBlank String category,

        @Schema(description = "선호 브랜드", example = "MCM")
        String brand,

        @Schema(description = "전하고 싶은 의미", example = "취업을 축하하고, 오래 사용할 수 있는 선물이면 좋겠어요")
        String meaning,

        @Schema(description = "선물 분위기(최대 2개)")
        @Size(max = 2, message = "분위기는 최대 2개까지 선택할 수 있습니다.")
        List<GiftMood> moods,

        @Schema(description = "선물자가 아는 상대 취향")
        GiverKnownTasteRequest giverKnownTaste
) {

    @Schema(description = "선물자가 아는 취향")
    public record GiverKnownTasteRequest(
            @Schema(description = "색상(콤마 구분)", example = "블랙,다크브라운") String colors,
            @Schema(description = "스타일", example = "심플") String style,
            @Schema(description = "피하고 싶은 취향", example = "큰 로고") String avoid,
            @Schema(description = "착용 방식", example = "숄더백") String wearStyle
    ) {
    }

    /** DTO → 엔티티 변환. */
    public GiftSession toEntity() {
        GiverKnownTaste taste = (giverKnownTaste == null) ? null : GiverKnownTaste.builder()
                .colors(giverKnownTaste.colors())
                .style(giverKnownTaste.style())
                .avoid(giverKnownTaste.avoid())
                .wearStyle(giverKnownTaste.wearStyle())
                .build();

        return GiftSession.builder()
                .relationship(relationship)
                .occasion(occasion)
                .budgetMin(budgetMin)
                .budgetMax(budgetMax)
                .category(category)
                .brand(brand)
                .meaning(meaning)
                .moods(moods)
                .giverKnownTaste(taste)
                .build();
    }
}
