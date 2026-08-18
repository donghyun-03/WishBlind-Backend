package com.example.wishBlind.ai.repository;

import com.example.wishBlind.ai.domain.LlmCallLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LlmCallLogRepository extends JpaRepository<LlmCallLog, Long> {
}
