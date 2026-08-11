package com.example.wishBlind.recipient.service;

import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.gift.repository.GiftSessionRepository;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import com.example.wishBlind.recipient.domain.*;
import com.example.wishBlind.recipient.dto.TasteFormResponse;
import com.example.wishBlind.recipient.dto.TasteFormResponse.Option;
import com.example.wishBlind.recipient.dto.TasteFormResponse.Step;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * 취향 테스트 문항을 카테고리에 맞춰 동적으로 생성한다.
 * 옷/의류 카테고리는 '착용 방식' 문항을 제외한다.
 */
@Service
@RequiredArgsConstructor
public class TasteFormService {

    private final GiftSessionRepository giftSessionRepository;

    @Transactional(readOnly = true)
    public TasteFormResponse buildForm(String token) {
        GiftSession session = giftSessionRepository.findByInviteToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE));
        return build(session.getCategory());
    }

    private TasteFormResponse build(String category) {
        List<Step> steps = new ArrayList<>();
        steps.add(new Step("colors", "선호하는 색상을 골라주세요", "MULTI", false,
                options(PreferColor.values(), PreferColor::getLabel)));
        steps.add(new Step("mood", "어떤 디자인 분위기를 선호하나요?", "SINGLE", false,
                options(PreferMood.values(), PreferMood::getLabel)));
        steps.add(new Step("material", "선호하는 소재를 알려주세요", "SINGLE", false,
                options(PreferMaterial.values(), PreferMaterial::getLabel)));
        steps.add(new Step("logoVisibility", "로고 노출은 어느 정도가 좋나요?", "SINGLE", false,
                options(LogoVisibility.values(), LogoVisibility::getLabel)));
        steps.add(new Step("size", "원하는 크기를 알려주세요", "SINGLE", false,
                options(PreferSize.values(), PreferSize::getLabel)));

        if (includeWearStyle(category)) {
            steps.add(new Step("wearStyle", "착용 방식을 알려주세요", "SINGLE", false,
                    options(WearStyle.values(), WearStyle::getLabel)));
        }

        steps.add(new Step("avoid", "피하고 싶은 요소를 골라주세요", "MULTI", true,
                options(AvoidFactor.values(), AvoidFactor::getLabel)));

        return new TasteFormResponse(category, steps);
    }

    /** 옷/의류는 착용 방식 문항 제외, 그 외(가방·목걸이 등)는 포함. */
    private boolean includeWearStyle(String category) {
        if (category == null) {
            return true;
        }
        String c = category.replaceAll("\\s", "");
        return !(c.contains("옷") || c.contains("의류"));
    }

    private <E extends Enum<E>> List<Option> options(E[] values, Function<E, String> labelFn) {
        return Arrays.stream(values)
                .map(v -> new Option(v.name(), labelFn.apply(v)))
                .toList();
    }
}
