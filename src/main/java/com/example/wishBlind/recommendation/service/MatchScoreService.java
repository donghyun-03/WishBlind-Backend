package com.example.wishBlind.recommendation.service;

import com.example.wishBlind.product.domain.Product;
import com.example.wishBlind.recipient.domain.*;
import com.example.wishBlind.recommendation.domain.TasteAnalysis;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 받는 사람 취향과 상품 속성을 대조해 일치율·별점·근거·고려할 점을 계산한다.
 * 전부 규칙 기반(코드) — AI 미사용.
 */
@Service
public class MatchScoreService {

    private static final int W_COLOR = 25;
    private static final int W_MOOD = 15;
    private static final int W_MATERIAL = 15;
    private static final int W_LOGO = 15;
    private static final int W_SIZE = 15;
    private static final int W_WEAR = 15;

    public MatchResult score(RecipientPreference pref, Product product) {
        int totalWeight = 0;
        int matchedWeight = 0;
        List<String> reasons = new ArrayList<>();
        List<String> considerations = new ArrayList<>();

        // 색상
        totalWeight += W_COLOR;
        boolean colorMatch = pref.getColors().contains(PreferColor.ANY)
                || pref.getColors().contains(product.getColor());
        int colorStars;
        if (colorMatch) {
            matchedWeight += W_COLOR;
            reasons.add(product.getColor().getLabel() + " 색상 취향 반영");
            colorStars = 5;
        } else {
            considerations.add("선호 색상과 다를 수 있어요 (" + product.getColor().getLabel() + ")");
            colorStars = 2;
        }

        // 분위기
        totalWeight += W_MOOD;
        boolean moodMatch = pref.getMood() == PreferMood.ANY || pref.getMood() == product.getMood();
        int styleStars;
        if (moodMatch) {
            matchedWeight += W_MOOD;
            reasons.add(product.getMood().getLabel() + " 분위기와 잘 맞음");
            styleStars = 5;
        } else {
            considerations.add("선호 분위기와 다소 다를 수 있어요");
            styleStars = 3;
        }

        // 소재
        totalWeight += W_MATERIAL;
        boolean materialMatch = pref.getMaterial() == product.getMaterial();
        if (materialMatch) {
            matchedWeight += W_MATERIAL;
            reasons.add(product.getMaterial().getLabel() + " 소재 선호 반영");
        } else {
            considerations.add("소재가 선호와 다를 수 있어요 (" + product.getMaterial().getLabel() + ")");
        }

        // 로고 노출
        totalWeight += W_LOGO;
        boolean logoMatch = logoAcceptable(pref.getLogoVisibility(), product.getLogoLevel());
        if (logoMatch) {
            matchedWeight += W_LOGO;
            reasons.add("로고 노출 정도가 취향에 맞음");
        } else {
            considerations.add("로고가 생각보다 눈에 띌 수 있어요");
        }

        // 크기
        totalWeight += W_SIZE;
        boolean sizeMatch = pref.getSize() == PreferSize.ANY || pref.getSize() == product.getSize();
        if (sizeMatch) {
            matchedWeight += W_SIZE;
            reasons.add("원하는 크기와 맞음");
        } else {
            considerations.add("크기가 생각과 다를 수 있어요 (" + product.getSize().getLabel() + ")");
        }

        // 착용 방식 (양쪽 다 값이 있을 때만 평가)
        if (product.getWearStyle() != null && pref.getWearStyle() != null) {
            totalWeight += W_WEAR;
            if (pref.getWearStyle() == product.getWearStyle()) {
                matchedWeight += W_WEAR;
                reasons.add("착용 방식이 취향에 맞음");
            } else {
                considerations.add("착용 방식이 다를 수 있어요");
            }
        }

        int practicalMatched = (materialMatch ? 1 : 0) + (logoMatch ? 1 : 0) + (sizeMatch ? 1 : 0);
        int practicalityStars = 2 + practicalMatched; // 2~5

        // 피하고 싶은 요소 패널티
        int penalty = applyAvoidPenalty(pref, product, considerations);

        int base = (int) Math.round(matchedWeight * 100.0 / totalWeight);
        int matchRate = Math.max(0, Math.min(100, base - penalty));

        TasteAnalysis analysis = TasteAnalysis.builder()
                .colorStars(colorStars)
                .styleStars(styleStars)
                .practicalityStars(practicalityStars)
                .build();

        return new MatchResult(matchRate, analysis, reasons, considerations, buildTags(matchRate));
    }

    /** 받는 사람이 원하는 로고 노출 수준이 상품보다 같거나 더 허용적이면 OK. */
    private boolean logoAcceptable(LogoVisibility pref, LogoVisibility product) {
        return pref.ordinal() >= product.ordinal();
    }

    private int applyAvoidPenalty(RecipientPreference pref, Product product, List<String> considerations) {
        int penalty = 0;
        for (AvoidFactor avoid : pref.getAvoid()) {
            switch (avoid) {
                case BIG_LOGO -> {
                    if (product.getLogoLevel() == LogoVisibility.VISIBLE) {
                        penalty += 10;
                        considerations.add("큰 로고를 피하고 싶어했는데 로고가 두드러져요");
                    }
                }
                case FLASHY_COLOR -> {
                    if (product.getColor() == PreferColor.COLOR_POINT) {
                        penalty += 10;
                        considerations.add("화려한 색상을 피하고 싶어했어요");
                    }
                }
                case SMALL_STORAGE -> {
                    if ("가방".equals(product.getCategory()) && product.getSize() == PreferSize.SMALL) {
                        penalty += 10;
                        considerations.add("수납공간이 작을 수 있어요");
                    }
                }
                case HARD_TO_CARE -> {
                    if (product.getMaterial() == PreferMaterial.LEATHER) {
                        penalty += 5;
                        considerations.add("가죽이라 관리가 필요할 수 있어요");
                    }
                }
                default -> {
                    // HEAVY, NONE 등은 상품 속성으로 판단할 수 없어 패널티 없음
                }
            }
        }
        return penalty;
    }

    private List<String> buildTags(int matchRate) {
        List<String> tags = new ArrayList<>();
        if (matchRate >= 85) {
            tags.add("취향 일치 높음");
        } else if (matchRate >= 70) {
            tags.add("취향 대체로 맞음");
        } else {
            tags.add("특별함 강조");
        }
        return tags;
    }
}
