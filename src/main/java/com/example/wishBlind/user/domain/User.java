package com.example.wishBlind.user.domain;

import com.example.wishBlind.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자(마이페이지). 로그인/인증(Spring Security)은 후순위 단계라
 * 지금은 프로필·알림 설정 CRUD 용도로만 쓴다.
 * (password는 인증 도입 시 BCrypt 해싱으로 교체 — 지금은 평문 placeholder)
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    @Column(unique = true)
    private String email;

    private String password;        // TODO: 인증 도입 시 해싱

    private String profileImageUrl;

    // 알림 설정
    private boolean notifyEnabled;        // 전체 알림
    private boolean notifyGiftProgress;   // 선물 진행 알림
    private boolean notifyTasteProgress;  // 취향 진행 알림

    @Builder
    public User(String nickname, String email, String password) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.notifyEnabled = true;
        this.notifyGiftProgress = true;
        this.notifyTasteProgress = true;
    }

    /** 마이페이지 정보 수정(프로필 + 알림 설정). password는 값이 있을 때만 변경. */
    public void updateProfile(String nickname, String email, String password, String profileImageUrl,
                              boolean notifyEnabled, boolean notifyGiftProgress, boolean notifyTasteProgress) {
        this.nickname = nickname;
        this.email = email;
        if (password != null && !password.isBlank()) {
            this.password = password;
        }
        this.profileImageUrl = profileImageUrl;
        this.notifyEnabled = notifyEnabled;
        this.notifyGiftProgress = notifyGiftProgress;
        this.notifyTasteProgress = notifyTasteProgress;
    }
}
