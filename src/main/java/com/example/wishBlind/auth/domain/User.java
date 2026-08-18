package com.example.wishBlind.auth.domain;

import com.example.wishBlind.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원.
 * 테이블명이 users인 이유: user는 MySQL 예약어라 그대로 쓰면 DDL이 깨진다.
 * email/passwordHash가 nullable인 이유: 소셜 전용 가입은 둘 다 없이 만들어진다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 255)
    private String email;

    @Column(length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    private LocalDateTime withdrawnAt;

    // 마이페이지: 프로필 이미지 + 알림 설정
    private String profileImageUrl;
    private boolean notifyEnabled;
    private boolean notifyGiftProgress;
    private boolean notifyTasteProgress;

    private User(String email, String passwordHash, String nickname) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.status = UserStatus.ACTIVE;
        this.notifyEnabled = true;
        this.notifyGiftProgress = true;
        this.notifyTasteProgress = true;
    }

    /** 자체 가입 */
    public static User ofLocal(String email, String passwordHash, String nickname) {
        return new User(email, passwordHash, nickname);
    }

    /** 소셜 전용 가입. 제공자가 이메일을 안 주는 경우가 있어 email은 null 허용. */
    public static User ofSocial(String email, String nickname) {
        return new User(email, null, nickname);
    }

    public boolean isWithdrawn() {
        return status == UserStatus.WITHDRAWN;
    }

    /**
     * 탈퇴. 물리 삭제하지 않는 이유: gifts 등 다른 테이블이 user_id를 참조하고 있어
     * 지우면 참조가 깨진다. 재가입 시 같은 이메일을 쓸 수 있도록 email은 비운다.
     */
    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
        this.email = null;
        this.passwordHash = null;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changePhone(String phone) {
        this.phone = phone;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /** 마이페이지 정보 수정(닉네임 + 프로필 이미지 + 알림 설정). */
    public void updateMyPage(String nickname, String profileImageUrl,
                             boolean notifyEnabled, boolean notifyGiftProgress, boolean notifyTasteProgress) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.notifyEnabled = notifyEnabled;
        this.notifyGiftProgress = notifyGiftProgress;
        this.notifyTasteProgress = notifyTasteProgress;
    }
}
