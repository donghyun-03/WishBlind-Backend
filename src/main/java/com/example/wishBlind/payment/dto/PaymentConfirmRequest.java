package com.example.wishBlind.payment.dto;

import com.example.wishBlind.payment.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "결제 승인 요청 (mock)")
public record PaymentConfirmRequest(

        @Schema(description = "주문 번호 (ready 응답값)", example = "ORD-AB12CD34")
        @NotBlank String orderId,

        @Schema(description = "결제 키 (ready 응답값)", example = "mock_xxx")
        @NotBlank String paymentKey,

        @Schema(description = "결제 수단", example = "CARD")
        PaymentMethod method,

        @Schema(description = "실패 시뮬레이션 (데모용). true면 결제 실패 처리", example = "false")
        boolean simulateFail
) {
}
