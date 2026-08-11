package com.example.wishBlind.recipient.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 착용 방식 (STEP 04, 카테고리에 따라 노출). */
@Getter
@RequiredArgsConstructor
public enum WearStyle {

    DELICATE("작고 섬세함"),
    MODERATE("적당한 존재감"),
    STATEMENT("포인트가 되는 크기");

    private final String label;
}
