package com.example.wishBlind.recipient.repository;

import com.example.wishBlind.recipient.domain.RecipientPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientPreferenceRepository extends JpaRepository<RecipientPreference, Long> {

    boolean existsByGiftSession_Id(Long giftSessionId);
}
