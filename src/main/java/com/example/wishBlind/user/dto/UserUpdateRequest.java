package com.example.wishBlind.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 마이페이지 정보 수정 요청 (프로필 + 알림 설정).
 * 이메일·비밀번호 변경은 인증(auth) 파트에서 처리하므로 여기서는 다루지 않는다.
 */
@Schema(description = "마이페이지 정보 수정 요청")
public record UserUpdateRequest(
        @Schema(description = "닉네임", example = "멋쟁이사자") @NotBlank String nickname,
        @Schema(description = "프로필 이미지 URL") String profileImageUrl,
        @Schema(description = "전체 알림", example = "true") boolean notifyEnabled,
        @Schema(description = "선물 진행 알림", example = "true") boolean notifyGiftProgress,
        @Schema(description = "취향 진행 알림", example = "true") boolean notifyTasteProgress
) {
}
