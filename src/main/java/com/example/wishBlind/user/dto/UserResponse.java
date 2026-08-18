package com.example.wishBlind.user.dto;

import com.example.wishBlind.auth.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자(마이페이지) 응답 — 비밀번호는 노출하지 않음")
public record UserResponse(
        Long id,
        String nickname,
        String email,
        String profileImageUrl,
        boolean notifyEnabled,
        boolean notifyGiftProgress,
        boolean notifyTasteProgress
) {

    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(),
                u.getNickname(),
                u.getEmail(),
                u.getProfileImageUrl(),
                u.isNotifyEnabled(),
                u.isNotifyGiftProgress(),
                u.isNotifyTasteProgress()
        );
    }
}
