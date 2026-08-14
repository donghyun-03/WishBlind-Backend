package com.example.wishBlind.payment.dto;

import com.example.wishBlind.payment.domain.Payment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "결제 상태 응답")
public record PaymentResponse(
        Long id,
        String orderId,
        Integer amount,
        String method,
        String methodLabel,
        String status,
        String statusLabel,
        LocalDateTime approvedAt
) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrderId(),
                p.getAmount(),
                p.getMethod() == null ? null : p.getMethod().name(),
                p.getMethod() == null ? null : p.getMethod().getLabel(),
                p.getStatus().name(),
                p.getStatus().getLabel(),
                p.getApprovedAt()
        );
    }
}
