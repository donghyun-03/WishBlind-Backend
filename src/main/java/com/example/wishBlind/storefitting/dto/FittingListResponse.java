package com.example.wishBlind.storefitting.dto;

import com.example.wishBlind.storefitting.domain.StoreFitting;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.format.DateTimeFormatter;

@Schema(description = "매장 체험 예약 카드(목록)")
public record FittingListResponse(
        Long id,
        @Schema(description = "시간대", example = "14:00 ~ 15:00") String timeRange,
        String customerName,
        @Schema(description = "카테고리", example = "가방") String category,
        @Schema(description = "목적", example = "취업 축하 선물") String purpose,
        String status,
        String statusLabel
) {

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    public static FittingListResponse from(StoreFitting f) {
        String range = f.getReserveTime() == null ? null
                : HM.format(f.getReserveTime()) + " ~ " + HM.format(f.getReserveTime().plusHours(1));
        return new FittingListResponse(
                f.getId(),
                range,
                f.getCustomerName(),
                f.getGiftSession().getCategory(),
                f.getGiftSession().getOccasion(),
                f.getStatus().name(),
                f.getStatus().getLabel()
        );
    }
}
