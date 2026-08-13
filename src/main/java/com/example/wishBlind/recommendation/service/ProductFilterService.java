package com.example.wishBlind.recommendation.service;

import com.example.wishBlind.gift.domain.GiftSession;
import com.example.wishBlind.product.domain.Product;
import com.example.wishBlind.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 추천 1차 규칙 필터. AI를 쓰지 않고 카테고리·예산·재고로 후보군을 좁힌다.
 */
@Service
@RequiredArgsConstructor
public class ProductFilterService {

    private final ProductRepository productRepository;

    public List<Product> filter(GiftSession session) {
        int min = session.getBudgetMin() == null ? 0 : session.getBudgetMin();
        int max = session.getBudgetMax() == null ? Integer.MAX_VALUE : session.getBudgetMax();
        return productRepository.findByCategoryAndPriceBetweenAndStockGreaterThan(
                session.getCategory(), min, max, 0);
    }
}
