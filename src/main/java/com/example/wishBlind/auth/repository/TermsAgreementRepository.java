package com.example.wishBlind.auth.repository;

import com.example.wishBlind.auth.domain.TermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long> {

    List<TermsAgreement> findAllByUserId(Long userId);
}
