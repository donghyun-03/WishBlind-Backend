package com.example.wishBlind.notification.controller;

import com.example.wishBlind.auth.jwt.AuthUser;
import com.example.wishBlind.global.common.ApiResponse;
import com.example.wishBlind.notification.dto.NotificationResponse;
import com.example.wishBlind.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notification", description = "인앱 알림 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록", description = "최신순으로 반환.")
    @GetMapping
    public ApiResponse<List<NotificationResponse>> list(@AuthUser Long userId) {
        return ApiResponse.success(notificationService.getList(userId));
    }

    @Operation(summary = "안 읽은 알림 개수", description = "종 아이콘 배지용.")
    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(@AuthUser Long userId) {
        return ApiResponse.success(notificationService.unreadCount(userId));
    }

    @Operation(summary = "알림 읽음 처리")
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> read(@AuthUser Long userId, @PathVariable Long id) {
        notificationService.markRead(id, userId);
        return ApiResponse.ok();
    }

    @Operation(summary = "전체 읽음 처리")
    @PatchMapping("/read-all")
    public ApiResponse<Void> readAll(@AuthUser Long userId) {
        notificationService.markAllRead(userId);
        return ApiResponse.ok();
    }
}
