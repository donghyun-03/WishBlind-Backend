package com.example.wishBlind.storefitting.service;

import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.gift.service.GiftSessionService;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import com.example.wishBlind.recommendation.domain.Recommendation;
import com.example.wishBlind.recommendation.repository.RecommendationRepository;
import com.example.wishBlind.storefitting.domain.FittingResult;
import com.example.wishBlind.storefitting.domain.StoreFitting;
import com.example.wishBlind.storefitting.dto.*;
import com.example.wishBlind.storefitting.repository.StoreFittingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreFittingService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final StoreFittingRepository fittingRepository;
    private final GiftSessionService giftSessionService;
    private final RecommendationRepository recommendationRepository;

    /** 체험 예약 생성. */
    @Transactional
    public FittingResponse create(FittingCreateRequest request) {
        GiftSession session = giftSessionService.findById(request.giftSessionId());
        StoreFitting fitting = fittingRepository.save(StoreFitting.builder()
                .giftSession(session)
                .customerName(request.customerName())
                .brand(session.getBrand())
                .reservationNumber(generateReservationNumber())
                .reserveDate(request.reserveDate())
                .reserveTime(request.reserveTime())
                .build());
        return FittingResponse.from(fitting);
    }

    /** 날짜별 예약 목록 (기본: 오늘). */
    public List<FittingListResponse> listByDate(LocalDate date) {
        LocalDate target = date == null ? LocalDate.now() : date;
        return fittingRepository.findByReserveDateOrderByReserveTimeAsc(target).stream()
                .map(FittingListResponse::from)
                .toList();
    }

    /** 예약 상세 (AI 추천 후보 포함). */
    public FittingDetailResponse getDetail(Long id) {
        StoreFitting fitting = findById(id);
        List<Recommendation> candidates =
                recommendationRepository.findByGiftSession_IdOrderByRankAsc(fitting.getGiftSession().getId());
        return FittingDetailResponse.of(fitting, candidates);
    }

    /** 체험 시작 → IN_PROGRESS. */
    @Transactional
    public FittingResponse start(Long id) {
        StoreFitting fitting = findById(id);
        fitting.start();
        return FittingResponse.from(fitting);
    }

    /** 체험 결과 저장 → DONE. 이 결과는 AI 추천 보정에 활용 가능. */
    @Transactional
    public FittingDetailResponse submitResult(Long id, FittingResultRequest request) {
        StoreFitting fitting = findById(id);
        fitting.complete(FittingResult.builder()
                .preferredCandidateProductId(request.preferredCandidateProductId())
                .materialFeel(request.materialFeel())
                .sizeFeel(request.sizeFeel())
                .storageFeel(request.storageFeel())
                .wearComfort(request.wearComfort())
                .weight(request.weight())
                .overallSatisfaction(request.overallSatisfaction())
                .staffMemo(request.staffMemo())
                .build());

        List<Recommendation> candidates =
                recommendationRepository.findByGiftSession_IdOrderByRankAsc(fitting.getGiftSession().getId());
        return FittingDetailResponse.of(fitting, candidates);
    }

    private StoreFitting findById(Long id) {
        return fittingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FITTING_NOT_FOUND));
    }

    private String generateReservationNumber() {
        char c1 = (char) ('A' + RANDOM.nextInt(26));
        char c2 = (char) ('A' + RANDOM.nextInt(26));
        int digits = 1000 + RANDOM.nextInt(9000);
        return "" + c1 + c2 + "-" + digits;
    }
}
