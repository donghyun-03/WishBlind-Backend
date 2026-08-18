package com.example.wishBlind.payment.domain;

import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결제 정보 (GiftSession 1:1). 실제 PG 연동 없이 데모용 mock.
 * 실제 결제 위젯 흐름(주문 생성 → 승인)을 흉내낸다.
 */
@Getter
@Entity
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_session_id", unique = true, nullable = false)
    private GiftSession giftSession;

    @Column(nullable = false, unique = true)
    private String orderId;        // 주문 번호(mock)

    @Column(nullable = false)
    private String paymentKey;     // 결제 키(mock)

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private LocalDateTime approvedAt;

    @Builder
    public Payment(GiftSession giftSession, String orderId, String paymentKey, Integer amount) {
        this.giftSession = giftSession;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.status = PaymentStatus.READY;
    }

    public void approve(PaymentMethod method) {
        this.method = method;
        this.status = PaymentStatus.PAID;
        this.approvedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
    }

    /** 재요청 시 금액·키 갱신(READY 상태 재사용). */
    public void reset(String paymentKey, Integer amount) {
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.status = PaymentStatus.READY;
        this.method = null;
        this.approvedAt = null;
    }
}
