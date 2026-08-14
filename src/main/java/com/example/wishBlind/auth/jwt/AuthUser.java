package com.example.wishBlind.auth.jwt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 인증된 회원 ID를 컨트롤러 파라미터로 주입한다.
 * 다른 기능 모듈이 인증과 맞닿는 유일한 접점.
 *
 * <pre>
 * {@code @GetMapping("/api/gifts")}
 * public ApiResponse&lt;List&lt;GiftSummary&gt;&gt; list(@AuthUser Long userId) { ... }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthUser {
}
