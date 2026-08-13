package com.example.wishBlind.delivery.domain;

import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 선물 전달 정보 (GiftSession 1:1). 최종 선택 후 입력.
 * 배송(SHIP): 배송지·이름·전화 / 매장 수령(STORE_PICKUP): 이름·예약 날짜·시간.
 */
@Getter
@Entity
@Table(name = "delivery")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_session_id", unique = true, nullable = false)
    private GiftSession giftSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryMethod method;

    @Column(columnDefinition = "TEXT")
    private String message;      // 축하 메시지

    private String recipientName;

    // 배송(SHIP)
    private String address;
    private String phone;

    // 매장 수령(STORE_PICKUP)
    private LocalDate reserveDate;
    private LocalTime reserveTime;

    @Builder
    public Delivery(GiftSession giftSession, DeliveryMethod method, String message, String recipientName,
                    String address, String phone, LocalDate reserveDate, LocalTime reserveTime) {
        this.giftSession = giftSession;
        this.method = method;
        this.message = message;
        this.recipientName = recipientName;
        this.address = address;
        this.phone = phone;
        this.reserveDate = reserveDate;
        this.reserveTime = reserveTime;
    }

    /** 재입력 시 값 갱신(메서드·필드 교체). */
    public void update(DeliveryMethod method, String message, String recipientName,
                       String address, String phone, LocalDate reserveDate, LocalTime reserveTime) {
        this.method = method;
        this.message = message;
        this.recipientName = recipientName;
        this.address = address;
        this.phone = phone;
        this.reserveDate = reserveDate;
        this.reserveTime = reserveTime;
    }
}
