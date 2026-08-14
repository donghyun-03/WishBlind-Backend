package com.example.wishBlind.delivery.dto;

import com.example.wishBlind.delivery.domain.Delivery;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "선물 전달 정보 응답")
public record DeliveryResponse(
        Long id,
        String method,
        String methodLabel,
        String message,
        String recipientName,
        String address,
        String phone,
        LocalDate reserveDate,
        LocalTime reserveTime,
        String giftStatus,
        String giftStatusLabel
) {

    public static DeliveryResponse from(Delivery d) {
        return new DeliveryResponse(
                d.getId(),
                d.getMethod().name(),
                d.getMethod().getLabel(),
                d.getMessage(),
                d.getRecipientName(),
                d.getAddress(),
                d.getPhone(),
                d.getReserveDate(),
                d.getReserveTime(),
                d.getGiftSession().getStatus().name(),
                d.getGiftSession().getStatus().getLabel()
        );
    }
}
