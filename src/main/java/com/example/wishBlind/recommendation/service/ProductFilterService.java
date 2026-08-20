package com.example.wishBlind.recommendation.service;

import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.product.domain.Product;
import com.example.wishBlind.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 추천 1차 규칙 필터. AI를 쓰지 않고 카테고리·예산·재고로 후보군을 좁힌다.
 *
 * 후보가 0개가 되면 추천 자체가 불가능해지므로, 엄격 조건에서 결과가 없으면
 * 단계적으로 조건을 완화한다. 카탈로그에 재고 있는 상품이 하나라도 있으면
 * 최소 1개 이상의 후보를 반드시 반환한다.
 */
@Service
@RequiredArgsConstructor
public class ProductFilterService {

    private final ProductRepository productRepository;

    public List<Product> filter(GiftSession session) {
        int min = session.getBudgetMin() == null ? 0 : session.getBudgetMin();
        int max = session.getBudgetMax() == null ? Integer.MAX_VALUE : session.getBudgetMax();
        String category = session.getCategory();

        // 1) 카테고리 + 예산 + 재고 (가장 엄격)
        List<Product> result = productRepository
                .findByCategoryAndPriceBetweenAndStockGreaterThan(category, min, max, 0);
        if (!result.isEmpty()) {
            return result;
        }

        // 2) 카테고리 + 재고 (예산 완화) — 예산을 벗어나도 같은 카테고리는 보여준다
        result = productRepository.findByCategoryAndStockGreaterThan(category, 0);
        if (!result.isEmpty()) {
            return result;
        }

        // 3) 예산 + 재고 (카테고리 완화) — 카테고리 상품이 없으면 예산대에 맞는 다른 카테고리
        result = productRepository.findByPriceBetweenAndStockGreaterThan(min, max, 0);
        if (!result.isEmpty()) {
            return result;
        }

        // 4) 재고 있는 아무 상품 (최후) — 카탈로그가 비어 있지 않는 한 항상 후보가 있다
        return productRepository.findByStockGreaterThan(0);
    }
}
