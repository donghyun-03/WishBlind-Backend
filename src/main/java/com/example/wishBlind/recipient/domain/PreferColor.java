package com.example.wishBlind.recipient.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 선호 색상 (STEP 01, 복수 선택). */
@Getter
@RequiredArgsConstructor
public enum PreferColor {

    BROWN("브라운"),
    BEIGE("베이지"),
    WHITE("화이트"),
    BLACK("블랙"),
    GREEN("그린"),
    COLOR_POINT("컬러 포인트"),
    ANY("상관없음");

    private final String label;
}
