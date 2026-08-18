package com.example.wishBlind.auth.domain;

/** 약관 종류. SERVICE/PRIVACY는 필수, MARKETING은 선택. */
public enum TermsType {
    SERVICE,
    PRIVACY,
    MARKETING;

    public boolean isRequired() {
        return this == SERVICE || this == PRIVACY;
    }
}
