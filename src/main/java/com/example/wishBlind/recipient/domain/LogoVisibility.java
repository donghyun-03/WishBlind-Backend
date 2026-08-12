package com.example.wishBlind.recipient.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 로고 노출 정도 (STEP 03). */
@Getter
@RequiredArgsConstructor
public enum LogoVisibility {

    NONE("거의 없음"),
    SUBTLE("작게 보임"),
    VISIBLE("눈에 띄어도 괜찮음");

    private final String label;
}
