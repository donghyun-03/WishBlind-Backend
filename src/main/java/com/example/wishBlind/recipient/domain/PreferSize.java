package com.example.wishBlind.recipient.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 원하는 크기 (STEP 04). */
@Getter
@RequiredArgsConstructor
public enum PreferSize {

    SMALL("작게"),
    BASIC("기본 길이"),
    LONG("길게"),
    ANY("상관없음");

    private final String label;
}
