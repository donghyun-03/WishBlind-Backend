package com.example.wishBlind.gift.dto;

import com.example.wishBlind.gift.domain.GiftMood;
import com.example.wishBlind.gift.domain.GiftSession;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "선물 세션 단건 응답")
public record GiftSessionResponse(
        Long id,
        String relationship,
        String occasion,
        Integer budgetMin,
        Integer budgetMax,
        String category,
        String brand,
        String meaning,
        List<GiftMood> moods,
        String status,
        String statusLabel,
        String inviteToken,
        String inviteCode,
        LocalDateTime createdAt
) {

    public static GiftSessionResponse from(GiftSession s) {
        return new GiftSessionResponse(
                s.getId(),
                s.getRelationship(),
                s.getOccasion(),
                s.getBudgetMin(),
                s.getBudgetMax(),
                s.getCategory(),
                s.getBrand(),
                s.getMeaning(),
                s.getMoods(),
                s.getStatus().name(),
                s.getStatus().getLabel(),
                s.getInviteToken(),
                s.getInviteCode(),
                s.getCreatedAt()
        );
    }
}
