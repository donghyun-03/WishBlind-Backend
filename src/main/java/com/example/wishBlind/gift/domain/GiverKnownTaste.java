package com.example.wishBlind.gift.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 선물하는 사람이 "알고 있는" 상대 취향(선물자 STEP 03).
 * 받는 사람이 직접 입력하는 블라인드 취향(RecipientPreference)과는 별개.
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GiverKnownTaste {

    @Column(name = "known_colors")
    private String colors;      // 콤마 구분 문자열

    @Column(name = "known_style")
    private String style;

    @Column(name = "known_avoid")
    private String avoid;

    @Column(name = "known_wear_style")
    private String wearStyle;

    @Builder
    public GiverKnownTaste(String colors, String style, String avoid, String wearStyle) {
        this.colors = colors;
        this.style = style;
        this.avoid = avoid;
        this.wearStyle = wearStyle;
    }
}
