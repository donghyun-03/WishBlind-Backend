package com.example.wishBlind.storefitting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 직원이 기록하는 체험 결과 (진행 4스텝 결과).
 * 고정 선택지는 프론트가 라벨/코드로 넘기고, 여기선 문자열로 저장한다.
 * 이 결과는 이후 AI 추천 보정 입력으로 활용할 수 있다.
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FittingResult {

    private Long preferredCandidateProductId;   // 고객이 가장 선호한 후보 상품

    private String materialFeel;      // 소재 (예: "부드러운 가죽 선호")
    private String sizeFeel;          // 크기 (예: "Small 선호")
    private String storageFeel;       // 수납감 (예: "보통")
    private String wearComfort;       // 착용감 (예: "불편함 호소")
    private String weight;            // 무게 (예: "가벼운 제품 선호")
    private String overallSatisfaction; // 전체 만족도 (예: "만족")

    @Column(length = 500)
    private String staffMemo;         // 직원 메모

    @Builder
    public FittingResult(Long preferredCandidateProductId, String materialFeel, String sizeFeel,
                         String storageFeel, String wearComfort, String weight,
                         String overallSatisfaction, String staffMemo) {
        this.preferredCandidateProductId = preferredCandidateProductId;
        this.materialFeel = materialFeel;
        this.sizeFeel = sizeFeel;
        this.storageFeel = storageFeel;
        this.wearComfort = wearComfort;
        this.weight = weight;
        this.overallSatisfaction = overallSatisfaction;
        this.staffMemo = staffMemo;
    }
}
