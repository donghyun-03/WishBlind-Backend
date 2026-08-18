package com.example.wishBlind.payment.controller;

import com.example.wishBlind.global.common.ApiResponse;
import com.example.wishBlind.payment.dto.PaymentConfirmRequest;
import com.example.wishBlind.payment.dto.PaymentReadyResponse;
import com.example.wishBlind.payment.dto.PaymentResponse;
import com.example.wishBlind.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment", description = "결제 API (데모용 mock, 실제 PG 미연동)")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 준비(mock)", description = "최종 선택한 상품 금액으로 주문을 생성한다. orderId·paymentKey 반환.")
    @PostMapping("/api/gift-sessions/{giftSessionId}/payment/ready")
    public ApiResponse<PaymentReadyResponse> ready(@PathVariable Long giftSessionId) {
        return ApiResponse.success(paymentService.ready(giftSessionId));
    }

    @Operation(summary = "결제 승인(mock)", description = "orderId·paymentKey로 승인 처리. simulateFail=true면 실패 데모.")
    @PostMapping("/api/gift-sessions/{giftSessionId}/payment/confirm")
    public ApiResponse<PaymentResponse> confirm(@PathVariable Long giftSessionId,
                                                @Valid @RequestBody PaymentConfirmRequest request) {
        return ApiResponse.success(paymentService.confirm(request));
    }

    @Operation(summary = "결제 상태 조회")
    @GetMapping("/api/gift-sessions/{giftSessionId}/payment")
    public ApiResponse<PaymentResponse> get(@PathVariable Long giftSessionId) {
        return ApiResponse.success(paymentService.get(giftSessionId));
    }

    @Operation(summary = "결제 취소(mock)")
    @PostMapping("/api/gift-sessions/{giftSessionId}/payment/cancel")
    public ApiResponse<PaymentResponse> cancel(@PathVariable Long giftSessionId) {
        return ApiResponse.success(paymentService.cancel(giftSessionId));
    }
}
