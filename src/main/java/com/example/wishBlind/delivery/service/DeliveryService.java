package com.example.wishBlind.delivery.service;

import com.example.wishBlind.delivery.domain.Delivery;
import com.example.wishBlind.delivery.domain.DeliveryMethod;
import com.example.wishBlind.delivery.dto.DeliveryRequest;
import com.example.wishBlind.delivery.dto.DeliveryResponse;
import com.example.wishBlind.delivery.dto.RevealResponse;
import com.example.wishBlind.delivery.repository.DeliveryRepository;
import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.gift.domain.GiftStatus;
import com.example.wishBlind.gift.repository.GiftSessionRepository;
import com.example.wishBlind.gift.service.GiftSessionService;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import com.example.wishBlind.product.domain.Product;
import com.example.wishBlind.recommendation.domain.Recommendation;
import com.example.wishBlind.recommendation.repository.RecommendationRepository;
import com.example.wishBlind.notification.domain.NotificationType;
import com.example.wishBlind.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final GiftSessionService giftSessionService;
    private final GiftSessionRepository giftSessionRepository;
    private final RecommendationRepository recommendationRepository;
    private final NotificationService notificationService;

    /** 전달 정보 입력/수정 → 상태 PREPARING(배송 준비 중). */
    @Transactional
    public DeliveryResponse submit(Long giftSessionId, DeliveryRequest request, Long userId) {
        GiftSession session = giftSessionService.findOwned(giftSessionId, userId);

        if (session.getStatus() != GiftStatus.FINALIZED && session.getStatus() != GiftStatus.PREPARING) {
            throw new BusinessException(ErrorCode.NOT_FINALIZED);
        }
        validate(request);

        Delivery delivery = deliveryRepository.findByGiftSession_Id(giftSessionId)
                .map(existing -> {
                    existing.update(request.method(), request.message(), request.recipientName(),
                            request.address(), request.phone(), request.reserveDate(), request.reserveTime());
                    return existing;
                })
                .orElseGet(() -> deliveryRepository.save(Delivery.builder()
                        .giftSession(session)
                        .method(request.method())
                        .message(request.message())
                        .recipientName(request.recipientName())
                        .address(request.address())
                        .phone(request.phone())
                        .reserveDate(request.reserveDate())
                        .reserveTime(request.reserveTime())
                        .build()));

        session.changeStatus(GiftStatus.PREPARING);
        notificationService.notify(userId, NotificationType.DELIVERY_STARTED,
                "선물이 배송 준비 중이에요.", giftSessionId);
        return DeliveryResponse.from(delivery);
    }

    public DeliveryResponse get(Long giftSessionId, Long userId) {
        giftSessionService.findOwned(giftSessionId, userId);
        Delivery delivery = deliveryRepository.findByGiftSession_Id(giftSessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));
        return DeliveryResponse.from(delivery);
    }

    /** 수령 완료 처리 → 상태 COMPLETED(선물 완료). 이 시점부터 공개 가능. */
    @Transactional
    public void complete(Long giftSessionId, Long userId) {
        GiftSession session = giftSessionService.findOwned(giftSessionId, userId);
        session.changeStatus(GiftStatus.COMPLETED);
        notificationService.notify(userId, NotificationType.GIFT_COMPLETED,
                "선물이 전달 완료됐어요!", giftSessionId);
    }

    /** 받는 사람 선물 공개. COMPLETED 이전에는 상품을 숨긴다. */
    public RevealResponse reveal(String token) {
        GiftSession session = giftSessionRepository.findByInviteToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE));

        if (session.getStatus() != GiftStatus.COMPLETED) {
            return RevealResponse.hidden();
        }

        Recommendation chosen = recommendationRepository.findByGiftSession_IdAndChosenTrue(session.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RECOMMENDATION_NOT_FOUND));
        Product product = chosen.getProduct();
        String message = deliveryRepository.findByGiftSession_Id(session.getId())
                .map(Delivery::getMessage)
                .orElse(null);

        return RevealResponse.revealed(
                product.getBrand(), product.getName(), product.getPrice(), product.getImageUrl(), message);
    }

    private void validate(DeliveryRequest request) {
        boolean valid = switch (request.method()) {
            case SHIP -> StringUtils.hasText(request.address()) && StringUtils.hasText(request.phone());
            case STORE_PICKUP -> request.reserveDate() != null && request.reserveTime() != null;
        };
        if (!valid) {
            throw new BusinessException(ErrorCode.DELIVERY_INFO_REQUIRED);
        }
    }
}
