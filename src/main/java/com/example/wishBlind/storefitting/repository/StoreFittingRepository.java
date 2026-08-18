package com.example.wishBlind.storefitting.repository;

import com.example.wishBlind.storefitting.domain.StoreFitting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StoreFittingRepository extends JpaRepository<StoreFitting, Long> {

    List<StoreFitting> findByReserveDateOrderByReserveTimeAsc(LocalDate reserveDate);
}
