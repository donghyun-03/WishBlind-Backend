package com.example.wishBlind.recipient.domain;

import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 받는 사람이 상품을 모른 채 입력하는 블라인드 취향 (GiftSession 1:1).
 * 취향 테스트 STEP 01~05 결과.
 */
@Getter
@Entity
@Table(name = "recipient_preference")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipientPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_session_id", unique = true, nullable = false)
    private GiftSession giftSession;

    // STEP 01 색상 (복수)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recipient_preference_color",
            joinColumns = @JoinColumn(name = "recipient_preference_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "color")
    private List<PreferColor> colors = new ArrayList<>();

    // STEP 02 분위기
    @Enumerated(EnumType.STRING)
    private PreferMood mood;

    // STEP 03 소재 + 로고 노출
    @Enumerated(EnumType.STRING)
    private PreferMaterial material;

    @Enumerated(EnumType.STRING)
    private LogoVisibility logoVisibility;

    // STEP 04 크기 + 착용 방식(카테고리에 따라 nullable)
    @Enumerated(EnumType.STRING)
    private PreferSize size;

    @Enumerated(EnumType.STRING)
    private WearStyle wearStyle;

    // STEP 05 피하고 싶은 요소 (복수) + 자유 입력
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recipient_preference_avoid",
            joinColumns = @JoinColumn(name = "recipient_preference_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "avoid_factor")
    private List<AvoidFactor> avoid = new ArrayList<>();

    @Column(length = 500)
    private String avoidEtc;

    @Builder
    public RecipientPreference(GiftSession giftSession, List<PreferColor> colors, PreferMood mood,
                               PreferMaterial material, LogoVisibility logoVisibility, PreferSize size,
                               WearStyle wearStyle, List<AvoidFactor> avoid, String avoidEtc) {
        this.giftSession = giftSession;
        if (colors != null) {
            this.colors = colors;
        }
        this.mood = mood;
        this.material = material;
        this.logoVisibility = logoVisibility;
        this.size = size;
        this.wearStyle = wearStyle;
        if (avoid != null) {
            this.avoid = avoid;
        }
        this.avoidEtc = avoidEtc;
    }
}
