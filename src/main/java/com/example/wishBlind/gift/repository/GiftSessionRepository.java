package com.example.wishBlind.gift.repository;

import com.example.wishBlind.gift.domain.GiftSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GiftSessionRepository extends JpaRepository<GiftSession, Long> {

    List<GiftSession> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<GiftSession> findByInviteToken(String inviteToken);

    Optional<GiftSession> findByInviteCode(String inviteCode);
}
