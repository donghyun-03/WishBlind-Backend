package com.example.wishBlind.ai.domain;

import com.example.wishBlind.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * LLM 호출 기록. 응답이 재현되지 않으므로 모델·토큰·소요시간을 남겨 사후 추적한다.
 * 프롬프트는 회원 취향이 들어 있어 원문 대신 해시만 저장한다.
 */
@Entity
@Table(name = "llm_call_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LlmCallLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(name = "prompt_hash", nullable = false, length = 64)
    private String promptHash;

    @Column(name = "latency_ms", nullable = false)
    private long latencyMs;

    @Column(name = "input_tokens")
    private Long inputTokens;

    @Column(name = "output_tokens")
    private Long outputTokens;

    @Column(nullable = false)
    private boolean success;

    /** 실패 시 ErrorCode.code (L001~). 성공이면 null. */
    @Column(name = "failure_code", length = 10)
    private String failureCode;

    private LlmCallLog(String model, String promptHash, long latencyMs,
                       Long inputTokens, Long outputTokens, boolean success, String failureCode) {
        this.model = model;
        this.promptHash = promptHash;
        this.latencyMs = latencyMs;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.success = success;
        this.failureCode = failureCode;
    }

    public static LlmCallLog success(String model, String promptHash, long latencyMs,
                                     Long inputTokens, Long outputTokens) {
        return new LlmCallLog(model, promptHash, latencyMs, inputTokens, outputTokens, true, null);
    }

    public static LlmCallLog failure(String model, String promptHash,
                                     long latencyMs, String failureCode) {
        return new LlmCallLog(model, promptHash, latencyMs, null, null, false, failureCode);
    }
}
