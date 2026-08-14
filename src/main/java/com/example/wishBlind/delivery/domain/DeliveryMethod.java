package com.example.wishBlind.delivery.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 전달 방법. */
@Getter
@RequiredArgsConstructor
public enum DeliveryMethod {

    SHIP("배송하기"),
    STORE_PICKUP("매장 방문 수령");

    private final String label;
}
