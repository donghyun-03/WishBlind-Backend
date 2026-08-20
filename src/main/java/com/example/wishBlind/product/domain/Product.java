package com.example.wishBlind.product.domain;

import com.example.wishBlind.global.common.BaseEntity;
import com.example.wishBlind.recipient.domain.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 브랜드 상품 카탈로그. 시드 데이터로 주입한다.
 * 상품 속성은 받는 사람 취향 enum(PreferColor 등)을 그대로 사용해,
 * 추천 단계의 취향-상품 매칭을 enum 비교로 단순하게 처리할 수 있게 한다.
 */
@Getter
@Entity
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String name;

    /** 카테고리(가방/옷/목걸이 등). 선물 세션의 category 문자열과 매칭. */
    private String category;

    private Integer price;

    @Enumerated(EnumType.STRING)
    private PreferColor color;

    @Enumerated(EnumType.STRING)
    private PreferMaterial material;

    @Enumerated(EnumType.STRING)
    private LogoVisibility logoLevel;

    @Enumerated(EnumType.STRING)
    private PreferSize size;

    @Enumerated(EnumType.STRING)
    private WearStyle wearStyle;

    @Enumerated(EnumType.STRING)
    private PreferMood mood;

    private String imageUrl;

    private Integer stock;

    @Builder
    public Product(String brand, String name, String category, Integer price,
                   PreferColor color, PreferMaterial material, LogoVisibility logoLevel,
                   PreferSize size, WearStyle wearStyle, PreferMood mood,
                   String imageUrl, Integer stock) {
        this.brand = brand;
        this.name = name;
        this.category = category;
        this.price = price;
        this.color = color;
        this.material = material;
        this.logoLevel = logoLevel;
        this.size = size;
        this.wearStyle = wearStyle;
        this.mood = mood;
        this.imageUrl = imageUrl;
        this.stock = stock;
    }

    /** 이미지 URL 교체(예: Pexels 실제 사진으로 보강). */
    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
