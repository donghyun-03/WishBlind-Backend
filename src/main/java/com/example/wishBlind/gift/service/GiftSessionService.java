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
    public GiftSessionResponse create(GiftSessionCreateRequest request, Long userId) {
        GiftSession saved = giftSessionRepository.save(request.toEntity(userId));
        return GiftSessionResponse.from(saved);
    }

    /** 단건 조회. 내 세션만 볼 수 있다. */
    public GiftSessionResponse get(Long id, Long userId) {
        return GiftSessionResponse.from(findOwned(id, userId));
    }

    /** 홈 대시보드 목록. 로그인한 회원이 만든 선물만 최신순으로 반환한다. */
    public List<GiftSessionListResponse> getMyList(Long userId) {
        return giftSessionRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(GiftSessionListResponse::from)
                .toList();
    }

    /** 내부 재사용용 엔티티 조회. 소유자를 확인하지 않으므로 인증 진입점에서 직접 쓰지 말 것. */
    public GiftSession findById(Long id) {
        return giftSessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GIFT_SESSION_NOT_FOUND));
    }

    /**
     * 소유자 확인까지 마친 엔티티 조회.
     *
     * 세션 ID는 연속된 정수라 남의 ID를 찍어보기 쉽다. 소유자를 확인하지 않으면
     * 로그인만 한 사람이 남의 선물 내역·추천·배송지를 열람하거나 바꿀 수 있다.
     * giftSessionId를 받는 인증 API는 전부 이 메서드를 거쳐야 한다.
     */
    public GiftSession findOwned(Long id, Long userId) {
        GiftSession session = findById(id);
        if (!session.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.GIFT_SESSION_FORBIDDEN);
        }
        return session;
    }
}
