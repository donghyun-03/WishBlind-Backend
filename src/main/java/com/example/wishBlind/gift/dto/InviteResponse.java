package com.example.wishBlind.gift.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "초대 링크/코드 응답 (선물자 STEP 04)")
public record InviteResponse(
        @Schema(description = "초대 토큰") String inviteToken,
        @Schema(description = "초대 코드(받는 사람이 입력)", example = "AB12CD34") String inviteCode,
        @Schema(description = "초대 링크 URL") String inviteUrl
) {
}
