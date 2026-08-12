package com.example.wishBlind.gift.service;

import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.gift.dto.InviteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 혼동되는 0,O,1,I 제외
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GiftSessionService giftSessionService;

    @Value("${app.invite-base-url}")
    private String inviteBaseUrl;

    /** 초대 토큰·코드 발급, 상태 INVITED 전환. */
    @Transactional
    public InviteResponse createInvite(Long giftSessionId) {
        GiftSession session = giftSessionService.findById(giftSessionId);

        String token = UUID.randomUUID().toString().replace("-", "");
        String code = generateCode();
        session.assignInvite(token, code);

        String url = inviteBaseUrl + "/" + token;
        return new InviteResponse(token, code, url);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
