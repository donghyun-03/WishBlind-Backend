package com.example.wishBlind.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 알림 종류. 각 종류는 카테고리(취향/선물)에 속해 사용자 설정으로 on/off 된다. */
@Getter
@RequiredArgsConstructor
public enum NotificationType {

    TASTE_SUBMITTED("취향 입력 완료", NotificationCategory.TASTE),
    RECOMMENDED("AI 추천 완료", NotificationCategory.GIFT),
    DELIVERY_STARTED("배송 준비 중", NotificationCategory.GIFT),
    GIFT_COMPLETED("선물 완료", NotificationCategory.GIFT);

    private final String label;
    private final NotificationCategory category;
}
