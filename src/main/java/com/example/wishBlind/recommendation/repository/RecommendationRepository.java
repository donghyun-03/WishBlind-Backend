package com.example.wishBlind.recommendation.repository;

import com.example.wishBlind.recommendation.domain.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findByGiftSession_IdOrderByRankAsc(Long giftSessionId);

    Optional<Recommendation> findByGiftSession_IdAndChosenTrue(Long giftSessionId);

    void deleteByGiftSession_Id(Long giftSessionId);
}
