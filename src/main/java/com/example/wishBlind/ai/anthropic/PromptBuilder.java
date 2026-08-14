package com.example.wishBlind.ai.anthropic;

import com.example.wishBlind.ai.AiRecommendCommand;
import com.example.wishBlind.ai.CandidateProduct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/** 선물 정보를 프롬프트 텍스트로 조립한다. */
@Component
public class PromptBuilder {

    private static final String SYSTEM = """
            당신은 선물 추천 전문가다. 선물하는 사람의 의도와 받는 사람의 취향을 함께 읽고,
            주어진 후보 상품 중에서 가장 적합한 3개를 고른다.

            지켜야 할 것:
            - 반드시 주어진 후보 목록 안의 productId만 사용한다. 목록에 없는 상품은 어떤 경우에도 만들지 않는다.
            - 예산 상한을 넘는 상품은 고르지 않는다.
            - 받는 사람이 "피하고 싶은 요소"로 꼽은 특성을 가진 상품은 고르지 않는다.
            - 정확히 3개를 rank 1, 2, 3으로 매긴다. rank 1이 가장 추천하는 상품이다.
            - 같은 상품을 두 번 고르지 않는다.

            각 추천에 대해:
            - matchScore는 받는 사람 취향과의 일치율을 0~100으로 매긴다.
            - reasons는 "선물 의미 적합", "선호 색상 반영", "스타일 적합", "예산 범위 만족",
              "브랜드 이미지 적합" 관점에서 해당하는 것만 짧은 문장으로 쓴다.
            - colorScore, styleScore, practicalityScore는 각각 색상 적합도, 스타일 적합도, 실용성을 0~100으로 매긴다.
            - considerations에는 사이즈나 착용·사용상 주의처럼 미리 알면 좋을 점을 쓴다. 없으면 빈 배열로 둔다.
            - 모든 문장은 한국어로, 선물하는 사람에게 말하듯 자연스럽게 쓴다.
            """;

    public String system() {
        return SYSTEM;
    }

    public String user(AiRecommendCommand command) {
        StringBuilder sb = new StringBuilder();

        sb.append("## 선물하는 사람의 의도\n");
        appendIfPresent(sb, "관계", command.relation());
        appendIfPresent(sb, "기념일", command.anniversaryType());
        sb.append("- 예산: ").append(command.budgetMin()).append("원 ~ ")
                .append(command.budgetMax()).append("원 (상한 초과 불가)\n");
        appendIfPresent(sb, "전하고 싶은 의미", command.meaningText());
        appendListIfPresent(sb, "선물 분위기", command.moods());

        sb.append("\n## 받는 사람의 취향\n");
        Map<String, List<String>> preferences = command.recipientPreferences();
        if (preferences == null || preferences.isEmpty()) {
            sb.append("- (응답 없음)\n");
        } else {
            preferences.forEach((question, answers) ->
                    sb.append("- ").append(question).append(": ")
                            .append(String.join(", ", answers)).append('\n'));
        }
        appendListIfPresent(sb, "피하고 싶은 요소", command.avoidElements());

        sb.append("\n## 후보 상품\n");
        for (CandidateProduct candidate : command.candidates()) {
            sb.append("- productId=").append(candidate.productId())
                    .append(" | ").append(candidate.brand())
                    .append(" | ").append(candidate.name())
                    .append(" | ").append(candidate.category())
                    .append(" | ").append(candidate.price()).append("원");
            if (candidate.attributes() != null && !candidate.attributes().isEmpty()) {
                StringJoiner attrs = new StringJoiner(", ", " | ", "");
                candidate.attributes().forEach((k, v) -> attrs.add(k + "=" + v));
                sb.append(attrs);
            }
            sb.append('\n');
        }

        sb.append("\n위 후보 중에서 3개를 골라 rank 1~3으로 추천해줘.");
        return sb.toString();
    }

    private void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append("- ").append(label).append(": ").append(value).append('\n');
        }
    }

    private void appendListIfPresent(StringBuilder sb, String label, List<String> values) {
        if (values != null && !values.isEmpty()) {
            sb.append("- ").append(label).append(": ").append(String.join(", ", values)).append('\n');
        }
    }
}
