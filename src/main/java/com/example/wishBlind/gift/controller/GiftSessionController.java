package com.example.wishBlind.gift.controller;

import com.example.wishBlind.auth.jwt.AuthUser;
import com.example.wishBlind.gift.dto.GiftSessionCreateRequest;
import com.example.wishBlind.gift.dto.GiftSessionListResponse;
import com.example.wishBlind.gift.dto.GiftSessionResponse;
import com.example.wishBlind.gift.dto.InviteResponse;
import com.example.wishBlind.gift.service.GiftSessionService;
import com.example.wishBlind.gift.service.InviteService;
import com.example.wishBlind.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Gift", description = "선물 세션 API (선물하는 사람)")
@RestController
@RequestMapping("/api/gift-sessions")
@RequiredArgsConstructor
public class GiftSessionController {

    private final GiftSessionService giftSessionService;
    private final InviteService inviteService;

    @Operation(summary = "선물 세션 생성", description = "선물자 입력(관계·기념일·예산·의미·분위기·아는 취향)으로 선물 건을 만든다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<GiftSessionResponse> create(@AuthUser Long userId,
                                                   @Valid @RequestBody GiftSessionCreateRequest request) {
        return ApiResponse.success(giftSessionService.create(request, userId));
    }

    @Operation(summary = "선물 세션 단건 조회", description = "선물 건 상세 및 현재 상태를 조회한다. 내 세션만 조회할 수 있다.")
    @GetMapping("/{id}")
    public ApiResponse<GiftSessionResponse> get(@AuthUser Long userId, @PathVariable Long id) {
        return ApiResponse.success(giftSessionService.get(id, userId));
    }

    @Operation(summary = "내 선물 목록(홈 대시보드)", description = "내가 만든 선물을 상태 배지와 함께 최신순으로 반환한다.")
    @GetMapping
    public ApiResponse<List<GiftSessionListResponse>> list(@AuthUser Long userId) {
        return ApiResponse.success(giftSessionService.getMyList(userId));
    }

    @Operation(summary = "초대 링크/코드 생성", description = "받는 사람에게 보낼 초대 토큰·코드·URL을 발급하고 상태를 '취향 입력 대기'로 전환한다.")
    @PostMapping("/{id}/invite")
    public ApiResponse<InviteResponse> invite(@AuthUser Long userId, @PathVariable Long id) {
        giftSessionService.findOwned(id, userId);
        return ApiResponse.success(inviteService.createInvite(id));
    }
}
