package com.example.wishBlind.delivery.repository;

import com.example.wishBlind.delivery.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByGiftSession_Id(Long giftSessionId);
}
