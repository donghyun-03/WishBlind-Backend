package com.example.wishBlind.auth.api;

import com.example.wishBlind.auth.api.dto.AuthDtos.LoginRequest;
import com.example.wishBlind.auth.api.dto.AuthDtos.RefreshRequest;
import com.example.wishBlind.auth.api.dto.AuthDtos.SignupRequest;
import com.example.wishBlind.auth.api.dto.AuthDtos.SocialLinkRequest;
import com.example.wishBlind.auth.api.dto.AuthDtos.SocialLoginRequest;
import com.example.wishBlind.auth.api.dto.AuthDtos.TermsAgreementRequest;
import com.example.wishBlind.auth.api.dto.AuthDtos.TokenResponse;
import com.example.wishBlind.auth.application.AuthService;
import com.example.wishBlind.auth.application.TermsAgreementCommand;
import com.example.wishBlind.auth.application.TokenService;
import com.example.wishBlind.auth.domain.OAuthProvider;
import com.example.wishBlind.auth.jwt.AuthUser;
import com.example.wishBlind.global.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 인증 API. 이 경로 전체가 SecurityConfig 화이트리스트에 있어야 한다. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/signup")
    public ApiResponse<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(TokenResponse.from(authService.signup(
                request.email(),
                request.password(),
                request.nickname(),
                request.phone(),
                toCommands(request.terms()))));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(
                TokenResponse.from(authService.login(request.email(), request.password())));
    }

    /** 소셜 로그인 겸 가입. 신규 가입이면 body에 필수 약관 동의가 함께 와야 한다. */
    @PostMapping("/social/{provider}")
    public ApiResponse<TokenResponse> socialLogin(@PathVariable OAuthProvider provider,
                                                  @Valid @RequestBody SocialLoginRequest request) {
        return ApiResponse.success(TokenResponse.from(
                authService.socialLogin(provider, request.accessToken(), toCommands(request.terms()))));
    }

    /** 로그인 상태에서 소셜 계정 연결. 인증이 필요하므로 화이트리스트에서 제외해야 한다. */
    @PostMapping("/social/link")
    public ApiResponse<Void> linkSocial(@AuthUser Long userId,
                                        @Valid @RequestBody SocialLinkRequest request) {
        authService.linkSocial(userId, request.provider(), request.accessToken());
        return ApiResponse.ok();
    }

    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(TokenResponse.from(tokenService.reissue(request.refreshToken())));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshRequest request) {
        tokenService.revoke(request.refreshToken());
        return ApiResponse.ok();
    }

    private List<TermsAgreementCommand> toCommands(List<TermsAgreementRequest> terms) {
        return (terms == null) ? List.of() : terms.stream().map(TermsAgreementRequest::toCommand).toList();
    }
}
