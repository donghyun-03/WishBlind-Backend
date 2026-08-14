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
 * LLM 호출 기록.
 *
 * 남기는 이유: LLM 응답은 같은 입력이라도 재현되지 않는다. "왜 이 상품이 추천됐나"를
 * 나중에 따지려면 호출 시점의 프롬프트 해시·모델·토큰·소요시간이 남아 있어야 한다.
 * 프롬프트 원문 대신 해시만 남긴다 — 원문에는 회원 취향 정보가 들어 있다.
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

    @Column(name = "candidate_count", nullable = false)
    private int candidateCount;

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

    private LlmCallLog(String model, String promptHash, int candidateCount, long latencyMs,
                       Long inputTokens, Long outputTokens, boolean success, String failureCode) {
        this.model = model;
        this.promptHash = promptHash;
        this.candidateCount = candidateCount;
        this.latencyMs = latencyMs;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.success = success;
        this.failureCode = failureCode;
    }

    public static LlmCallLog success(String model, String promptHash, int candidateCount,
                                     long latencyMs, Long inputTokens, Long outputTokens) {
        return new LlmCallLog(model, promptHash, candidateCount, latencyMs,
                inputTokens, outputTokens, true, null);
    }

    public static LlmCallLog failure(String model, String promptHash, int candidateCount,
                                     long latencyMs, String failureCode) {
        return new LlmCallLog(model, promptHash, candidateCount, latencyMs,
                null, null, false, failureCode);
    }
}
