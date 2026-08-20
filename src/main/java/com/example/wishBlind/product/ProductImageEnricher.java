package com.example.wishBlind.product;

import com.example.wishBlind.product.domain.Product;
import com.example.wishBlind.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 상품 이미지를 Pexels 실제 사진으로 보강한다(시드 이후 1회).
 * PEXELS_API_KEY 가 없으면 조용히 건너뛰고 placeholder 이미지를 유지한다.
 * 이미 Pexels 사진으로 교체된 상품(placeholder가 아닌 것)은 다시 호출하지 않는다.
 */
@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class ProductImageEnricher implements CommandLineRunner {

    /** 카테고리 → Pexels 검색 키워드(영문). */
    private static final Map<String, String> QUERY = Map.of(
            "가방", "handbag",
            "옷", "fashion clothing",
            "목걸이", "necklace jewelry",
            "지갑", "wallet",
            "신발", "shoes",
            "시계", "watch",
            "향수", "perfume",
            "스카프", "scarf"
    );

    private final ProductRepository productRepository;

    @Value("${pexels.api-key:}")
    private String pexelsApiKey;

    @Override
    @Transactional
    public void run(String... args) {
        if (pexelsApiKey == null || pexelsApiKey.isBlank()) {
            log.info("[ProductImageEnricher] PEXELS_API_KEY 없음 — 이미지 보강 건너뜀(placeholder 유지)");
            return;
        }

        List<Product> targets = productRepository.findAll().stream()
                .filter(ProductImageEnricher::needsRealImage)
                .toList();
        if (targets.isEmpty()) {
            return;
        }

        RestClient client = RestClient.builder()
                .baseUrl("https://api.pexels.com/v1")
                .defaultHeader("Authorization", pexelsApiKey)
                .build();

        Map<String, Integer> pageByQuery = new HashMap<>();
        int updated = 0;
        for (Product p : targets) {
            String query = QUERY.getOrDefault(p.getCategory(), "luxury gift");
            int page = pageByQuery.merge(query, 1, Integer::sum); // 같은 키워드는 페이지를 늘려 다른 사진
            try {
                String url = fetchImage(client, query, page);
                if (url != null) {
                    p.updateImageUrl(url);
                    updated++;
                }
            } catch (Exception e) {
                log.warn("[ProductImageEnricher] '{}' 이미지 조회 실패: {}", p.getName(), e.getMessage());
            }
        }
        log.info("[ProductImageEnricher] Pexels 이미지 {}건 보강 완료", updated);
    }

    private static boolean needsRealImage(Product p) {
        String url = p.getImageUrl();
        return url == null || url.contains("loremflickr") || url.contains("picsum");
    }

    @SuppressWarnings("unchecked")
    private String fetchImage(RestClient client, String query, int page) {
        Map<String, Object> res = client.get()
                .uri(uri -> uri.path("/search")
                        .queryParam("query", query)
                        .queryParam("per_page", 1)
                        .queryParam("page", page)
                        .queryParam("orientation", "square")
                        .build())
                .retrieve()
                .body(Map.class);

        if (res == null) {
            return null;
        }
        List<Map<String, Object>> photos = (List<Map<String, Object>>) res.get("photos");
        if (photos == null || photos.isEmpty()) {
            return null;
        }
        Map<String, Object> src = (Map<String, Object>) photos.get(0).get("src");
        return src == null ? null : (String) src.get("large");
    }
}
