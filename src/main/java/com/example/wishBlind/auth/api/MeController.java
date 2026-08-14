package com.example.wishBlind.auth.api;

import com.example.wishBlind.auth.api.dto.AuthDtos.MeResponse;
import com.example.wishBlind.auth.application.AuthService;
import com.example.wishBlind.auth.jwt.AuthUser;
import com.example.wishBlind.global.common.ApiResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 내 정보 / 탈퇴. 인증 필요. */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final AuthService authService;

    public MeController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<MeResponse> me(@AuthUser Long userId) {
        return ApiResponse.success(MeResponse.of(
                authService.findActiveUser(userId),
                authService.findLinkedProviders(userId)));
    }

    @DeleteMapping
    public ApiResponse<Void> withdraw(@AuthUser Long userId) {
        authService.withdraw(userId);
        return ApiResponse.ok();
    }
}
