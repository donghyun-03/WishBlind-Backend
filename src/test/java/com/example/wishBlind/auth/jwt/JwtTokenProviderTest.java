package com.example.wishBlind.auth.jwt;

import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-for-hs256-at-least-32-bytes";
    private static final long ACCESS_VALIDITY_SECONDS = 1800;

    private final JwtTokenProvider provider =
            new JwtTokenProvider(new JwtProperties(SECRET, ACCESS_VALIDITY_SECONDS, 1209600));

    @Test
    @DisplayName("발급한 토큰에서 회원 ID를 다시 꺼낼 수 있다")
    void parsesUserIdFromIssuedToken() {
        String token = provider.createAccessToken(42L);

        assertThat(provider.parseUserId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("만료된 토큰은 EXPIRED_TOKEN으로 거부한다")
    void rejectsExpiredToken() {
        // 유효기간(30분)보다 더 과거에 발급된 것으로 만들어 이미 만료된 토큰을 얻는다
        Instant longAgo = Instant.now().minus(ACCESS_VALIDITY_SECONDS + 60, ChronoUnit.SECONDS);
        String expired = provider.createAccessToken(42L, longAgo);

        assertThatThrownBy(() -> provider.parseUserId(expired))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.EXPIRED_TOKEN);
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 INVALID_TOKEN으로 거부한다")
    void rejectsTokenSignedWithAnotherKey() {
        JwtTokenProvider attacker = new JwtTokenProvider(
                new JwtProperties("another-secret-key-that-is-also-32-bytes-long", 1800, 1209600));
        String forged = attacker.createAccessToken(42L);

        assertThatThrownBy(() -> provider.parseUserId(forged))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("JWT 형식이 아닌 문자열은 INVALID_TOKEN으로 거부한다")
    void rejectsMalformedToken() {
        assertThatThrownBy(() -> provider.parseUserId("not-a-jwt"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("32바이트 미만 secret이면 기동 시점에 실패한다")
    void rejectsShortSecretAtStartup() {
        assertThatThrownBy(() -> new JwtTokenProvider(new JwtProperties("too-short", 1800, 1209600)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }
}
