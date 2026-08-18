package com.example.wishBlind.recommendation.controller;

import com.example.wishBlind.auth.jwt.AuthUser;
import com.example.wishBlind.global.common.ApiResponse;
import com.example.wishBlind.recommendation.dto.FinalizeRequest;
import com.example.wishBlind.recommendation.dto.RecommendationDetailResponse;
import com.example.wishBlind.recommendation.dto.RecommendationResponse;
import com.example.wishBlind.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recommendation", description = "AI 추천 · 최종 선택 API (선물하는 사람)")
@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(summary = "AI 추천 생성", description = "두 사람 정보를 결합해 후보 3개를 생성한다. 상태를 'AI 추천 완료'로 전환.")
    @PostMapping("/api/gift-sessions/{giftSessionId}/recommendations")
    public ApiResponse<List<RecommendationResponse>> generate(@AuthUser Long userId,
                                                              @PathVariable Long giftSessionId) {
        return ApiResponse.success(recommendationService.generate(giftSessionId, userId));
    }

    @Operation(summary = "AI 추천 목록 조회", description = "후보 3개(BEST·일치율·태그)를 순위대로 반환.")
    @GetMapping("/api/gift-sessions/{giftSessionId}/recommendations")
    public ApiResponse<List<RecommendationResponse>> list(@AuthUser Long userId,
                                                          @PathVariable Long giftSessionId) {
        return ApiResponse.success(recommendationService.getList(giftSessionId, userId));
    }

    @Operation(summary = "AI 추천 상세 조회", description = "추천 이유·별점·AI 코멘트·고려할 점을 반환.")
    @GetMapping("/api/recommendations/{recommendationId}")
    public ApiResponse<RecommendationDetailResponse> detail(@AuthUser Long userId,
                                                            @PathVariable Long recommendationId) {
        return ApiResponse.success(recommendationService.getDetail(recommendationId, userId));
    }

    @Operation(summary = "최종 상품 선택", description = "후보 중 하나를 최종 선택하고 상태를 '선물 선택 완료'로 전환.")
    @PostMapping("/api/gift-sessions/{giftSessionId}/finalize")
    public ApiResponse<Void> finalizeSelection(@AuthUser Long userId,
                                               @PathVariable Long giftSessionId,
                                               @Valid @RequestBody FinalizeRequest request) {
        recommendationService.finalizeSelection(giftSessionId, request.recommendationId(), userId);
        return ApiResponse.ok();
    }
}
