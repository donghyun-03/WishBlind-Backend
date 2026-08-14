package com.example.wishBlind.recipient.repository;

import com.example.wishBlind.recipient.domain.RecipientPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipientPreferenceRepository extends JpaRepository<RecipientPreference, Long> {

    boolean existsByGiftSession_Id(Long giftSessionId);

    Optional<RecipientPreference> findByGiftSession_Id(Long giftSessionId);
}
