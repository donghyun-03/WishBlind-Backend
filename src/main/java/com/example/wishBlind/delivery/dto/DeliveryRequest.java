package com.example.wishBlind.delivery.dto;

import com.example.wishBlind.delivery.domain.DeliveryMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "선물 전달 정보 입력 요청")
public record DeliveryRequest(

        @Schema(description = "전달 방법", example = "SHIP")
        @NotNull DeliveryMethod method,

        @Schema(description = "축하 메시지", example = "취업 축하해! 앞으로도 응원할게.")
        String message,

        @Schema(description = "받는 사람 이름", example = "김미소")
        @NotBlank String recipientName,

        // 배송(SHIP)
        @Schema(description = "배송지 (배송 시 필수)", example = "서울시 강남구 ...")
        String address,
        @Schema(description = "전화번호 (배송 시 필수)", example = "010-1234-5678")
        String phone,

        // 매장 수령(STORE_PICKUP)
        @Schema(description = "예약 날짜 (매장 수령 시 필수)", example = "2026-08-20")
        LocalDate reserveDate,
        @Schema(description = "예약 시간 (매장 수령 시 필수)", example = "14:30")
        LocalTime reserveTime
) {
}
