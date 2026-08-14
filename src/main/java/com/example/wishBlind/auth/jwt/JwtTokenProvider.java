package com.example.wishBlind.auth.jwt;

import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * access token 발급/검증.
 * refresh token은 JWT가 아니라 불투명 랜덤 문자열이다 — 폐기를 DB로 관리하므로
 * 토큰 자체에 상태를 담을 이유가 없다. TokenService 참고.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final Duration accessTokenValidity;

    public JwtTokenProvider(JwtProperties properties) {
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be at least 32 bytes for HS256 (current: " + secretBytes.length + ")");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.accessTokenValidity = Duration.ofSeconds(properties.accessTokenValidity());
    }

    public String createAccessToken(Long userId) {
        return createAccessToken(userId, Instant.now());
    }

    /** 테스트에서 만료 시나리오를 만들기 위해 기준 시각을 주입받는 오버로드. */
    public String createAccessToken(Long userId, Instant issuedAt) {
        Instant expiresAt = issuedAt.plus(accessTokenValidity);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    /**
     * 토큰에서 회원 ID를 꺼낸다.
     *
     * @throws BusinessException 만료(EXPIRED_TOKEN) 또는 서명·형식 오류(INVALID_TOKEN)
     */
    public Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
