package com.example.wishBlind.recipient.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 선호 분위기 (STEP 02). */
@Getter
@RequiredArgsConstructor
public enum PreferMood {

    SIMPLE("심플한"),
    MODERN("모던한"),
    TRENDY("트렌디한"),
    GLAMOROUS("화려한"),
    CLASSIC("클래식한"),
    ANY("상관없음");

    private final String label;
}
