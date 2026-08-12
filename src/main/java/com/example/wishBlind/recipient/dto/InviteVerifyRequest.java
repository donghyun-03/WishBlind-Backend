package com.example.wishBlind.recipient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "초대 코드 확인 요청 (받는 사람 초대 확인 화면)")
public record InviteVerifyRequest(
        @Schema(description = "초대 코드", example = "AB12CD34")
        @NotBlank String code
) {
}
