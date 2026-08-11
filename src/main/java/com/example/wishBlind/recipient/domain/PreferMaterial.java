package com.example.wishBlind.recipient.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 선호 소재 (STEP 03). */
@Getter
@RequiredArgsConstructor
public enum PreferMaterial {

    FABRIC("패브릭"),
    LEATHER("가죽"),
    METAL("메탈"),
    ETC("기타");

    private final String label;
}
