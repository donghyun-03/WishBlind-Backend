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
 * 약관 동의 이력.
 * 덮어쓰지 않고 append만 한다 — 개인정보 처리 동의는 "언제 어느 버전에 동의했는지"가
 * 증빙 대상이라 최신 상태만 남기면 의미가 없다.
 */
@Entity
@Table(name = "terms_agreements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "terms_type", nullable = false, length = 20)
    private TermsType termsType;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    private TermsAgreement(Long userId, TermsType termsType, String version) {
        this.userId = userId;
        this.termsType = termsType;
        this.version = version;
        this.agreedAt = LocalDateTime.now();
    }

    public static TermsAgreement of(Long userId, TermsType termsType, String version) {
        return new TermsAgreement(userId, termsType, version);
    }
}
