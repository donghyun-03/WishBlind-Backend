package com.example.wishBlind.storefitting.domain;

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
 * 매장 체험 예약 및 결과 (직원 페이지). 선물 세션의 후보 상품을 오프라인에서 체험한다.
 */
@Getter
@Entity
@Table(name = "store_fitting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreFitting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_session_id", nullable = false)
    private GiftSession giftSession;

    private String customerName;         // 고객명 (예: 김사자)
    private String brand;                // 브랜드
    @Column(unique = true)
    private String reservationNumber;    // 예약 번호 (예: AA-1234)

    private LocalDate reserveDate;
    private LocalTime reserveTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FittingStatus status;

    @Embedded
    private FittingResult result;

    @Builder
    public StoreFitting(GiftSession giftSession, String customerName, String brand,
                        String reservationNumber, LocalDate reserveDate, LocalTime reserveTime) {
        this.giftSession = giftSession;
        this.customerName = customerName;
        this.brand = brand;
        this.reservationNumber = reservationNumber;
        this.reserveDate = reserveDate;
        this.reserveTime = reserveTime;
        this.status = FittingStatus.WAITING;
    }

    public void start() {
        this.status = FittingStatus.IN_PROGRESS;
    }

    public void complete(FittingResult result) {
        this.result = result;
        this.status = FittingStatus.DONE;
    }

    public void cancel() {
        this.status = FittingStatus.CANCELED;
    }
}
