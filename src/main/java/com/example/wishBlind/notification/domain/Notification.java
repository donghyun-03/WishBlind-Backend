package com.example.wishBlind.notification.domain;

import com.example.wishBlind.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인앱 알림 한 건. 특정 사용자(userId, 보통 선물하는 사람)에게 쌓인다.
 * 읽음 여부와 관련 선물 세션(gift_session_id)을 함께 저장해 목록/딥링크에 쓴다.
 */
@Getter
@Entity
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    private String title;

    private String message;

    private Long giftSessionId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Builder
    public Notification(Long userId, NotificationType type, String title, String message, Long giftSessionId) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.giftSessionId = giftSessionId;
        this.read = false;
    }

    public void markRead() {
        this.read = true;
    }
}
