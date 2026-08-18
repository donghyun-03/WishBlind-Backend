package com.example.wishBlind.delivery.controller;

import com.example.wishBlind.auth.jwt.AuthUser;
import com.example.wishBlind.delivery.dto.DeliveryRequest;
import com.example.wishBlind.delivery.dto.DeliveryResponse;
import com.example.wishBlind.delivery.dto.RevealResponse;
import com.example.wishBlind.delivery.service.DeliveryService;
import com.example.wishBlind.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Delivery", description = "선물 전달 · 수령 · 공개 API")
@RestController
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Operation(summary = "전달 정보 입력", description = "배송(배송지·전화) 또는 매장 수령(예약 날짜·시간)과 메시지를 저장하고 상태를 '배송 준비 중'으로 전환.")
    @PostMapping("/api/gift-sessions/{giftSessionId}/delivery")
    public ApiResponse<DeliveryResponse> submit(@AuthUser Long userId,
                                                @PathVariable Long giftSessionId,
                                                @Valid @RequestBody DeliveryRequest request) {
        return ApiResponse.success(deliveryService.submit(giftSessionId, request, userId));
    }

    @Operation(summary = "전달 정보 조회")
    @GetMapping("/api/gift-sessions/{giftSessionId}/delivery")
    public ApiResponse<DeliveryResponse> get(@AuthUser Long userId, @PathVariable Long giftSessionId) {
        return ApiResponse.success(deliveryService.get(giftSessionId, userId));
    }

    @Operation(summary = "수령 완료 처리", description = "선물 수령을 완료 처리하고 상태를 '선물 완료'로 전환. 이후 공개 가능.")
    @PostMapping("/api/gift-sessions/{giftSessionId}/complete")
    public ApiResponse<Void> complete(@AuthUser Long userId, @PathVariable Long giftSessionId) {
        deliveryService.complete(giftSessionId, userId);
        return ApiResponse.ok();
    }

    @Operation(summary = "선물 공개", description = "받는 사람이 최종 상품을 확인한다. '선물 완료' 전까지는 상품을 숨긴다.")
    @GetMapping("/api/invite/{token}/reveal")
    public ApiResponse<RevealResponse> reveal(@PathVariable String token) {
        return ApiResponse.success(deliveryService.reveal(token));
    }
}
