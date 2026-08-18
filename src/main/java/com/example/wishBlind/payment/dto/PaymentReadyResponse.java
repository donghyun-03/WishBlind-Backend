package com.example.wishBlind.payment.dto;

import com.example.wishBlind.payment.domain.Payment;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 준비 응답 (mock 주문 생성)")
public record PaymentReadyResponse(
        @Schema(description = "주문 번호") String orderId,
        @Schema(description = "결제 키(mock)") String paymentKey,
        @Schema(description = "결제 금액") Integer amount,
        @Schema(description = "mock 결제창 URL") String checkoutUrl,
        @Schema(description = "상태") String status
) {

    public static PaymentReadyResponse from(Payment p) {
        return new PaymentReadyResponse(
                p.getOrderId(),
                p.getPaymentKey(),
                p.getAmount(),
                "https://mock-pay.wishblind.local/checkout/" + p.getOrderId(),
                p.getStatus().name()
        );
    }
}
