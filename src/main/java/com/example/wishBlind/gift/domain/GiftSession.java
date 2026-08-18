package com.example.wishBlind.gift.domain;

import com.example.wishBlind.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 선물 건 하나 = 홈 대시보드 카드 1개.
 * 선물하는 사람의 입력(관계·기념일·예산·의미·분위기·아는 취향)과 상태·초대 정보를 담는다.
 * 생성/수정 시각은 BaseEntity(JPA Auditing)에서 자동 관리.
 */
@Getter
@Entity
@Table(name = "gift_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GiftSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 선물하는 사람(회원) ID. auth 모듈의 User 엔티티를 직접 참조하지 않고 ID만 들고 있다.
    // 연관관계를 걸면 gift 모듈이 auth 모듈에 묶인다.
    //
    // DDL 상으로는 nullable이다. ddl-auto=update는 행이 이미 있는 테이블에 NOT NULL 컬럼을
    // 안전하게 붙이지 못한다. 값은 생성자에서 강제하고, NOT NULL 제약은 Flyway 도입 시 건다.
    @Column(name = "user_id")
    private Long userId;

    // STEP 01 기본 정보
    private String relationship;   // 관계
    private String occasion;       // 기념일/목적
    private Integer budgetMin;     // 예산 하한
    private Integer budgetMax;     // 예산 상한
    private String category;       // 카테고리(가방/옷/목걸이 등)
    private String brand;          // 선호 브랜드

    // STEP 02 선물 의미
    @Column(columnDefinition = "TEXT")
    private String meaning;        // 전하고 싶은 의미

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gift_session_mood",
            joinColumns = @JoinColumn(name = "gift_session_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "mood")
    private List<GiftMood> moods = new ArrayList<>();  // 최대 2개

    // STEP 03 선물자가 아는 취향
    @Embedded
    private GiverKnownTaste giverKnownTaste;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GiftStatus status;

    @Column(unique = true)
    private String inviteToken;
    private String inviteCode;

    @Builder
    public GiftSession(Long userId, String relationship, String occasion, Integer budgetMin, Integer budgetMax,
                       String category, String brand, String meaning, List<GiftMood> moods,
                       GiverKnownTaste giverKnownTaste) {
        if (userId == null) {
            throw new IllegalArgumentException("선물 세션에는 소유자(userId)가 반드시 있어야 한다.");
        }
        this.userId = userId;
        this.relationship = relationship;
        this.occasion = occasion;
        this.budgetMin = budgetMin;
        this.budgetMax = budgetMax;
        this.category = category;
        this.brand = brand;
        this.meaning = meaning;
        if (moods != null) {
            this.moods = moods;
        }
        this.giverKnownTaste = giverKnownTaste;
        this.status = GiftStatus.CREATED;
    }

    /** 초대 발급 시 토큰·코드를 저장하고 상태를 INVITED 로 전환. */
    public void assignInvite(String inviteToken, String inviteCode) {
        this.inviteToken = inviteToken;
        this.inviteCode = inviteCode;
        this.status = GiftStatus.INVITED;
    }

    public void changeStatus(GiftStatus status) {
        this.status = status;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }
}
