package com.example.wishBlind.recipient.service;

import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.gift.domain.GiftStatus;
import com.example.wishBlind.gift.repository.GiftSessionRepository;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import com.example.wishBlind.recipient.domain.RecipientPreference;
import com.example.wishBlind.recipient.dto.InviteInfoResponse;
import com.example.wishBlind.recipient.dto.PreferenceSubmitRequest;
import com.example.wishBlind.recipient.repository.RecipientPreferenceRepository;
import com.example.wishBlind.notification.domain.NotificationType;
import com.example.wishBlind.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipientService {

    private final GiftSessionRepository giftSessionRepository;
    private final RecipientPreferenceRepository preferenceRepository;
    private final NotificationService notificationService;

    /** 초대 링크(토큰)로 확인. */
    public InviteInfoResponse getInviteInfoByToken(String token) {
        return InviteInfoResponse.from(findByToken(token));
    }

    /** 초대 코드 입력으로 확인. */
    public InviteInfoResponse getInviteInfoByCode(String code) {
        GiftSession session = giftSessionRepository.findByInviteCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE));
        return InviteInfoResponse.from(session);
    }

    /** 블라인드 취향 저장 → 상태 ANALYZING(AI 분석 중) 전환. */
    @Transactional
    public void submitPreferences(String token, PreferenceSubmitRequest request) {
        GiftSession session = findByToken(token);

        if (preferenceRepository.existsByGiftSession_Id(session.getId())) {
            throw new BusinessException(ErrorCode.PREFERENCE_ALREADY_SUBMITTED);
        }

        RecipientPreference preference = RecipientPreference.builder()
                .giftSession(session)
                .colors(request.colors())
                .mood(request.mood())
                .material(request.material())
                .logoVisibility(request.logoVisibility())
                .size(request.size())
                .wearStyle(request.wearStyle())
                .avoid(request.avoid())
                .avoidEtc(request.avoidEtc())
                .build();

        preferenceRepository.save(preference);
        session.changeStatus(GiftStatus.ANALYZING);

        // 선물하는 사람에게 취향 입력 완료 알림
        notificationService.notify(session.getUserId(), NotificationType.TASTE_SUBMITTED,
                "받는 분이 취향 입력을 완료했어요. 이제 AI가 분석합니다.", session.getId());
    }

    private GiftSession findByToken(String token) {
        return giftSessionRepository.findByInviteToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE));
    }
}
