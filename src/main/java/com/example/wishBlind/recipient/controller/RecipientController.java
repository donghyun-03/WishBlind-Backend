package com.example.wishBlind.recipient.controller;

import com.example.wishBlind.global.common.ApiResponse;
import com.example.wishBlind.recipient.dto.InviteInfoResponse;
import com.example.wishBlind.recipient.dto.InviteVerifyRequest;
import com.example.wishBlind.recipient.dto.PreferenceSubmitRequest;
import com.example.wishBlind.recipient.dto.TasteFormResponse;
import com.example.wishBlind.recipient.service.RecipientService;
import com.example.wishBlind.recipient.service.TasteFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recipient", description = "초대 확인 · 블라인드 취향 테스트 API (받는 사람)")
@RestController
@RequestMapping("/api/invite")
@RequiredArgsConstructor
public class RecipientController {

    private final RecipientService recipientService;
    private final TasteFormService tasteFormService;

    @Operation(summary = "초대 확인(링크/토큰)", description = "초대 토큰으로 세션을 확인한다. 브랜드·카테고리만 노출(블라인드).")
    @GetMapping("/{token}")
    public ApiResponse<InviteInfoResponse> info(@PathVariable String token) {
        return ApiResponse.success(recipientService.getInviteInfoByToken(token));
    }

    @Operation(summary = "초대 확인(코드 입력)", description = "받는 사람이 초대 코드를 직접 입력해 확인한다.")
    @PostMapping("/verify")
    public ApiResponse<InviteInfoResponse> verify(@Valid @RequestBody InviteVerifyRequest request) {
        return ApiResponse.success(recipientService.getInviteInfoByCode(request.code()));
    }

    @Operation(summary = "취향 테스트 폼 조회", description = "카테고리에 맞춰 동적으로 구성된 취향 문항을 반환한다.")
    @GetMapping("/{token}/taste-form")
    public ApiResponse<TasteFormResponse> tasteForm(@PathVariable String token) {
        return ApiResponse.success(tasteFormService.buildForm(token));
    }

    @Operation(summary = "블라인드 취향 제출", description = "받는 사람의 취향을 저장하고 상태를 'AI 분석 중'으로 전환한다.")
    @PostMapping("/{token}/preferences")
    public ApiResponse<Void> submit(@PathVariable String token,
                                    @Valid @RequestBody PreferenceSubmitRequest request) {
        recipientService.submitPreferences(token, request);
        return ApiResponse.ok();
    }
}
