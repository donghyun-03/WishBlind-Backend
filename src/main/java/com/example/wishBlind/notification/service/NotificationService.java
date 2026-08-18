package com.example.wishBlind.notification.service;

import com.example.wishBlind.auth.domain.User;
import com.example.wishBlind.auth.repository.UserRepository;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import com.example.wishBlind.notification.domain.Notification;
import com.example.wishBlind.notification.domain.NotificationType;
import com.example.wishBlind.notification.dto.NotificationResponse;
import com.example.wishBlind.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 알림 생성. 사용자의 알림 설정(전체/취향/선물)을 존중해, 꺼둔 종류는 저장하지 않는다.
     * 이벤트 훅에서 호출한다.
     */
    @Transactional
    public void notify(Long userId, NotificationType type, String message, Long giftSessionId) {
        if (userId == null) {
            return;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isNotifyEnabled()) {
            return;
        }
        boolean allowed = switch (type.getCategory()) {
            case TASTE -> user.isNotifyTasteProgress();
            case GIFT -> user.isNotifyGiftProgress();
        };
        if (!allowed) {
            return;
        }

        notificationRepository.save(Notification.builder()
                .userId(userId)
                .type(type)
                .title(type.getLabel())
                .message(message)
                .giftSessionId(giftSessionId)
                .build());
    }

    public List<NotificationResponse> getList(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        notification.markRead();
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .forEach(Notification::markRead);
    }
}
