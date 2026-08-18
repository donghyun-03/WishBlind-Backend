package com.example.wishBlind.auth.domain;

/** 회원 상태. 탈퇴는 물리 삭제하지 않고 WITHDRAWN으로 표시한다. */
public enum UserStatus {
    ACTIVE,
    WITHDRAWN
}
