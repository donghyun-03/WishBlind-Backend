package com.example.wishBlind.auth.application;

import com.example.wishBlind.auth.domain.RefreshToken;
import com.example.wishBlind.auth.jwt.JwtProperties;
import com.example.wishBlind.auth.jwt.JwtTokenProvider;
import com.example.wishBlind.auth.repository.RefreshTokenRepository;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

/**
 * access/refresh 토큰의 발급·재발급·폐기.
 * refresh는 폐기를 DB로 관리하므로 JWT가 아닌 랜덤 문자열이고, DB에는 SHA-256 해시만 저장한다.
 */
@Service
public class TokenService {

    private static final int REFRESH_TOKEN_BYTES = 32;

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public TokenService(JwtTokenProvider tokenProvider,
                        RefreshTokenRepository refreshTokenRepository,
                        JwtProperties jwtProperties) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenValidity = jwtProperties.accessTokenValidity();
        this.refreshTokenValidity = jwtProperties.refreshTokenValidity();
    }

    @Transactional
    public TokenPair issue(Long userId) {
        String rawRefreshToken = generateRefreshToken();
        refreshTokenRepository.save(RefreshToken.of(
                userId,
                hash(rawRefreshToken),
                LocalDateTime.now().plusSeconds(refreshTokenValidity)));

        return new TokenPair(tokenProvider.createAccessToken(userId), rawRefreshToken, accessTokenValidity);
    }

    /**
     * 재발급. 사용한 refresh token은 즉시 폐기한다(rotation).
     * 폐기하지 않으면 탈취된 토큰이 만료까지 계속 유효하다.
     */
    @Transactional
    public TokenPair reissue(String rawRefreshToken) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (!stored.isUsable(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        stored.revoke();
        return issue(stored.getUserId());
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .ifPresent(RefreshToken::revoke);
    }

    /** 탈퇴 시 해당 회원의 모든 세션을 끊는다. */
    @Transactional
    public void revokeAll(Long userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(userId);
        tokens.forEach(RefreshToken::revoke);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** SHA-256 hex. 길이 64로 고정돼 refresh_tokens.token_hash 컬럼 길이와 맞는다. */
    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
