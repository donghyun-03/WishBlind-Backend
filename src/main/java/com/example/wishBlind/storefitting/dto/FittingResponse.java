package com.example.wishBlind.storefitting.dto;

import com.example.wishBlind.storefitting.domain.StoreFitting;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "매장 체험 예약 응답(간단)")
public record FittingResponse(
        Long id,
        Long giftSessionId,
        String customerName,
        String brand,
        String reservationNumber,
        LocalDate reserveDate,
        LocalTime reserveTime,
        String status,
        String statusLabel
) {

    public static FittingResponse from(StoreFitting f) {
        return new FittingResponse(
                f.getId(),
                f.getGiftSession().getId(),
                f.getCustomerName(),
                f.getBrand(),
                f.getReservationNumber(),
                f.getReserveDate(),
                f.getReserveTime(),
                f.getStatus().name(),
                f.getStatus().getLabel()
        );
    }
}
