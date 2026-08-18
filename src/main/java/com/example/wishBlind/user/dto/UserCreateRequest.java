package com.example.wishBlind.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "사용자 생성 요청 (인증 도입 전 임시 가입)")
public record UserCreateRequest(
        @Schema(description = "닉네임", example = "멋쟁이사자") @NotBlank String nickname,
        @Schema(description = "이메일", example = "aa12345@naver.com") @NotBlank @Email String email,
        @Schema(description = "비밀번호") @NotBlank String password
) {
}
