package com.example.wishBlind.product.repository;

import com.example.wishBlind.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    /**
     * 추천 1차 규칙 필터용: 카테고리 + 예산 구간 + 재고 있음.
     * (recommendation 단계에서 사용)
     */
    List<Product> findByCategoryAndPriceBetweenAndStockGreaterThan(
            String category, int priceMin, int priceMax, int stock);
}
