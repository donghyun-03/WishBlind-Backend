package com.example.wishBlind.ai.anthropic;

import com.example.wishBlind.ai.dto.AiRecommendationCommand;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 추천 코멘트 생성 프롬프트 조립.
 *
 * 후보 선정·점수·근거는 이미 규칙(MatchScoreService)이 계산해서 넘어온다.
 * LLM은 그 결과를 사람이 읽을 문장으로 옮기는 일만 한다 — 새 사실을 만들지 않게 하는 게 핵심.
 */
@Component
public class PromptBuilder {

    private static final String SYSTEM = """
            당신은 선물 추천 코멘트를 쓰는 사람이다.
            이미 계산된 추천 근거를 바탕으로, 선물하는 사람에게 건네는 한국어 코멘트를 쓴다.

            지켜야 할 것:
            - 주어진 근거 안에서만 쓴다. 상품 사양이나 가격처럼 주어지지 않은 사실을 지어내지 않는다.
            - 2~3문장, 200자 이내로 쓴다.
            - 일치율 숫자를 그대로 읊지 말고, 왜 잘 맞는지를 말한다.
            - '고려할 점'이 있으면 마지막에 한 문장으로 짚어준다.
            - 광고 문구처럼 과장하지 않는다. 담백하게 쓴다.
            - 코멘트 본문만 출력한다. 제목, 머리말, 따옴표, 내부 태그를 붙이지 않는다.
            """;

    public String system() {
        return SYSTEM;
    }

    public String user(AiRecommendationCommand command) {
        StringBuilder sb = new StringBuilder();
        appendIfPresent(sb, "기념일/목적", command.occasion());
        appendIfPresent(sb, "전하고 싶은 의미", command.meaning());
        appendListIfPresent(sb, "선물 분위기", command.moods());
        appendIfPresent(sb, "추천 상품", command.productName());
        sb.append("- 취향 일치율: ").append(command.matchRate()).append("%\n");
        appendListIfPresent(sb, "매칭 근거", command.reasons());
        appendListIfPresent(sb, "고려할 점", command.considerations());
        sb.append("\n이 선물을 추천하는 코멘트를 써줘.");
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
