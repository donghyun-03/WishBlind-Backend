package com.example.wishBlind.auth.api.dto;

import com.example.wishBlind.auth.application.TermsAgreementCommand;
import com.example.wishBlind.auth.application.TokenPair;
import com.example.wishBlind.auth.domain.OAuthProvider;
import com.example.wishBlind.auth.domain.TermsType;
import com.example.wishBlind.auth.domain.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 인증 API의 요청/응답 DTO 모음. 하나하나가 짧아 한 파일에 둔다. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record TermsAgreementRequest(
            @NotNull TermsType termsType,
            @NotBlank String version,
            boolean agreed
    ) {
        public TermsAgreementCommand toCommand() {
            return new TermsAgreementCommand(termsType, version, agreed);
        }
    }

    public record SignupRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 64) String password,
            @NotBlank @Size(max = 50) String nickname,
            @Size(max = 20) String phone,
            @NotNull List<@Valid TermsAgreementRequest> terms
    ) {
    }

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String password
    ) {
    }

    /** 클라이언트가 카카오/구글 SDK로 발급받은 access token을 그대로 넘긴다. */
    public record SocialLoginRequest(
            @NotBlank String accessToken,
            List<@Valid TermsAgreementRequest> terms
    ) {
    }

    public record SocialLinkRequest(
            @NotNull OAuthProvider provider,
            @NotBlank String accessToken
    ) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record TokenResponse(String accessToken, String refreshToken, long accessTokenExpiresIn) {
        public static TokenResponse from(TokenPair pair) {
            return new TokenResponse(pair.accessToken(), pair.refreshToken(), pair.accessTokenExpiresIn());
        }
    }

    public record MeResponse(
            Long id,
            String email,
            String nickname,
            String phone,
            List<OAuthProvider> linkedProviders
    ) {
        public static MeResponse of(User user, List<OAuthProvider> linkedProviders) {
            return new MeResponse(
                    user.getId(), user.getEmail(), user.getNickname(), user.getPhone(), linkedProviders);
        }
    }
}
