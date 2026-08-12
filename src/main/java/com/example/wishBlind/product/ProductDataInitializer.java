package com.example.wishBlind.product;

import com.example.wishBlind.product.domain.Product;
import com.example.wishBlind.product.repository.ProductRepository;
import com.example.wishBlind.recipient.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 앱 시작 시 상품 카탈로그가 비어 있으면 브랜드 라인업 시드 데이터를 주입한다.
 * (해커톤 데모/추천 매칭용 샘플)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        List<Product> seed = List.of(
                // 가방
                Product.builder().brand("MCM").name("비세토스 숄더백").category("가방").price(650000)
                        .color(PreferColor.BROWN).material(PreferMaterial.LEATHER).logoLevel(LogoVisibility.SUBTLE)
                        .size(PreferSize.BASIC).wearStyle(WearStyle.MODERATE).mood(PreferMood.CLASSIC)
                        .imageUrl("https://picsum.photos/seed/bag1/400").stock(5).build(),
                Product.builder().brand("MCM").name("미니 토트백").category("가방").price(480000)
                        .color(PreferColor.BLACK).material(PreferMaterial.LEATHER).logoLevel(LogoVisibility.SUBTLE)
                        .size(PreferSize.SMALL).wearStyle(WearStyle.DELICATE).mood(PreferMood.MODERN)
                        .imageUrl("https://picsum.photos/seed/bag2/400").stock(8).build(),
                Product.builder().brand("MCM").name("클래식 크로스백").category("가방").price(520000)
                        .color(PreferColor.BEIGE).material(PreferMaterial.FABRIC).logoLevel(LogoVisibility.VISIBLE)
                        .size(PreferSize.SMALL).wearStyle(WearStyle.MODERATE).mood(PreferMood.TRENDY)
                        .imageUrl("https://picsum.photos/seed/bag3/400").stock(3).build(),
                Product.builder().brand("MCM").name("대형 백팩").category("가방").price(720000)
                        .color(PreferColor.BLACK).material(PreferMaterial.LEATHER).logoLevel(LogoVisibility.VISIBLE)
                        .size(PreferSize.LONG).wearStyle(WearStyle.STATEMENT).mood(PreferMood.MODERN)
                        .imageUrl("https://picsum.photos/seed/bag4/400").stock(4).build(),
                Product.builder().brand("MCM").name("스몰 버킷백").category("가방").price(560000)
                        .color(PreferColor.WHITE).material(PreferMaterial.LEATHER).logoLevel(LogoVisibility.NONE)
                        .size(PreferSize.SMALL).wearStyle(WearStyle.DELICATE).mood(PreferMood.SIMPLE)
                        .imageUrl("https://picsum.photos/seed/bag5/400").stock(6).build(),

                // 옷
                Product.builder().brand("MCM").name("로고 티셔츠").category("옷").price(320000)
                        .color(PreferColor.WHITE).material(PreferMaterial.FABRIC).logoLevel(LogoVisibility.VISIBLE)
                        .size(PreferSize.BASIC).wearStyle(null).mood(PreferMood.TRENDY)
                        .imageUrl("https://picsum.photos/seed/cloth1/400").stock(10).build(),
                Product.builder().brand("MCM").name("캐시미어 니트").category("옷").price(450000)
                        .color(PreferColor.BROWN).material(PreferMaterial.FABRIC).logoLevel(LogoVisibility.NONE)
                        .size(PreferSize.BASIC).wearStyle(null).mood(PreferMood.CLASSIC)
                        .imageUrl("https://picsum.photos/seed/cloth2/400").stock(7).build(),

                // 목걸이
                Product.builder().brand("MCM").name("펜던트 목걸이").category("목걸이").price(380000)
                        .color(PreferColor.WHITE).material(PreferMaterial.METAL).logoLevel(LogoVisibility.SUBTLE)
                        .size(PreferSize.BASIC).wearStyle(WearStyle.DELICATE).mood(PreferMood.MODERN)
                        .imageUrl("https://picsum.photos/seed/neck1/400").stock(9).build(),
                Product.builder().brand("MCM").name("체인 목걸이").category("목걸이").price(420000)
                        .color(PreferColor.COLOR_POINT).material(PreferMaterial.METAL).logoLevel(LogoVisibility.VISIBLE)
                        .size(PreferSize.LONG).wearStyle(WearStyle.STATEMENT).mood(PreferMood.GLAMOROUS)
                        .imageUrl("https://picsum.photos/seed/neck2/400").stock(5).build()
        );

        productRepository.saveAll(seed);
        log.info("[ProductDataInitializer] 상품 시드 {}건 주입 완료", seed.size());
    }
}
