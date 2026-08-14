package com.example.wishBlind.payment.repository;

import com.example.wishBlind.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByGiftSession_Id(Long giftSessionId);

    Optional<Payment> findByOrderId(String orderId);
}
