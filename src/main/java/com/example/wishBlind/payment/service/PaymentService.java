package com.example.wishBlind.payment.service;

import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.gift.service.GiftSessionService;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import com.example.wishBlind.payment.domain.Payment;
import com.example.wishBlind.payment.domain.PaymentMethod;
import com.example.wishBlind.payment.domain.PaymentStatus;
import com.example.wishBlind.payment.dto.PaymentConfirmRequest;
import com.example.wishBlind.payment.dto.PaymentReadyResponse;
import com.example.wishBlind.payment.dto.PaymentResponse;
import com.example.wishBlind.payment.repository.PaymentRepository;
import com.example.wishBlind.recommendation.domain.Recommendation;
import com.example.wishBlind.recommendation.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 데모용 mock 결제. 실제 PG 연동 없음.
 * ready(주문 생성) → confirm(승인) 흐름을 흉내내며, 기본은 성공 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final GiftSessionService giftSessionService;
    private final RecommendationRepository recommendationRepository;

    /** 결제 준비: 최종 선택한 상품 가격으로 주문 생성(READY). */
    @Transactional
    public PaymentReadyResponse ready(Long giftSessionId) {
        GiftSession session = giftSessionService.findById(giftSessionId);

        Recommendation chosen = recommendationRepository.findByGiftSession_IdAndChosenTrue(giftSessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FINALIZED));
        int amount = chosen.getProduct().getPrice();

        String paymentKey = "mock_" + UUID.randomUUID().toString().replace("-", "");

        Payment payment = paymentRepository.findByGiftSession_Id(giftSessionId)
                .map(existing -> {
                    if (existing.getStatus() == PaymentStatus.PAID) {
                        throw new BusinessException(ErrorCode.PAYMENT_ALREADY_PAID);
                    }
                    existing.reset(paymentKey, amount);
                    return existing;
                })
                .orElseGet(() -> paymentRepository.save(Payment.builder()
                        .giftSession(session)
                        .orderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .paymentKey(paymentKey)
                        .amount(amount)
                        .build()));

        return PaymentReadyResponse.from(payment);
    }

    /** 결제 승인(mock): 키 검증 후 PAID 처리. simulateFail=true면 실패 처리. */
    @Transactional
    public PaymentResponse confirm(PaymentConfirmRequest request) {
        Payment payment = paymentRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_PAID);
        }
        if (!payment.getPaymentKey().equals(request.paymentKey())) {
            throw new BusinessException(ErrorCode.PAYMENT_KEY_MISMATCH);
        }
        if (request.simulateFail()) {
            payment.fail();
            return PaymentResponse.from(payment);
        }

        PaymentMethod method = request.method() == null ? PaymentMethod.CARD : request.method();
        payment.approve(method);
        return PaymentResponse.from(payment);
    }

    public PaymentResponse get(Long giftSessionId) {
        Payment payment = paymentRepository.findByGiftSession_Id(giftSessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResponse.from(payment);
    }

    /** 결제 취소(mock). */
    @Transactional
    public PaymentResponse cancel(Long giftSessionId) {
        Payment payment = paymentRepository.findByGiftSession_Id(giftSessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        payment.cancel();
        return PaymentResponse.from(payment);
    }
}
