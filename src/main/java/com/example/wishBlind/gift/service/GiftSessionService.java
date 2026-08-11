package com.example.wishBlind.gift.service;

import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.gift.dto.GiftSessionCreateRequest;
import com.example.wishBlind.gift.dto.GiftSessionListResponse;
import com.example.wishBlind.gift.dto.GiftSessionResponse;
import com.example.wishBlind.gift.repository.GiftSessionRepository;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GiftSessionService {

    private final GiftSessionRepository giftSessionRepository;

    /** 선물 세션 생성 (선물자 입력). */
    @Transactional
    public GiftSessionResponse create(GiftSessionCreateRequest request) {
        GiftSession saved = giftSessionRepository.save(request.toEntity());
        return GiftSessionResponse.from(saved);
    }

    /** 단건 조회. */
    public GiftSessionResponse get(Long id) {
        return GiftSessionResponse.from(findById(id));
    }

    /**
     * 홈 대시보드 목록. 인증 붙기 전이므로 전체를 최신순으로 반환.
     * (단계 10에서 사용자별 필터로 교체)
     */
    public List<GiftSessionListResponse> getMyList() {
        return giftSessionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(GiftSessionListResponse::from)
                .toList();
    }

    /** 내부 재사용용 엔티티 조회. */
    public GiftSession findById(Long id) {
        return giftSessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GIFT_SESSION_NOT_FOUND));
    }
}
