package com.example.wishBlind.product;

import com.example.wishBlind.product.domain.Product;
import com.example.wishBlind.product.repository.ProductRepository;
import com.example.wishBlind.recipient.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * 앱 시작 시 상품 카탈로그가 비어 있으면 resources/data/products.json 을 읽어 시드로 주입한다.
 * 상품을 늘리려면 자바 코드가 아니라 products.json 만 수정하면 된다.
 */
@Slf4j
@Order(1)   // 상품 시드가 먼저, 이미지 보강(ProductImageEnricher, @Order(2))이 그 뒤에 돈다
@Component
@RequiredArgsConstructor
public class ProductDataInitializer implements CommandLineRunner {

    private static final String SEED_PATH = "data/products.json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() > 0) {
            return;
        }

        List<ProductSeed> seeds;
        try (InputStream is = new ClassPathResource(SEED_PATH).getInputStream()) {
            seeds = OBJECT_MAPPER.readValue(is, new TypeReference<List<ProductSeed>>() {});
        }

        List<Product> products = seeds.stream()
                .map(ProductSeed::toEntity)
                .toList();

        productRepository.saveAll(products);
        log.info("[ProductDataInitializer] 상품 시드 {}건 주입 완료 ({})", products.size(), SEED_PATH);
    }

    /** products.json 한 항목 매핑용. 열거형은 Jackson이 문자열 → enum 으로 자동 변환한다. */
    private record ProductSeed(
            String brand,
            String name,
            String category,
            Integer price,
            PreferColor color,
            PreferMaterial material,
            LogoVisibility logoLevel,
            PreferSize size,
            WearStyle wearStyle,
            PreferMood mood,
            String imageUrl,
            Integer stock
    ) {
        Product toEntity() {
            return Product.builder()
                    .brand(brand)
                    .name(name)
                    .category(category)
                    .price(price)
                    .color(color)
                    .material(material)
                    .logoLevel(logoLevel)
                    .size(size)
                    .wearStyle(wearStyle)
                    .mood(mood)
                    .imageUrl(imageUrl)
                    .stock(stock)
                    .build();
        }
    }
}
