package com.example.wishBlind.product.dto;

import com.example.wishBlind.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 응답")
public record ProductResponse(
        Long id,
        String brand,
        String name,
        String category,
        Integer price,
        String color,
        String material,
        String logoLevel,
        String size,
        String wearStyle,
        String mood,
        String imageUrl,
        Integer stock
) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getBrand(),
                p.getName(),
                p.getCategory(),
                p.getPrice(),
                p.getColor() == null ? null : p.getColor().getLabel(),
                p.getMaterial() == null ? null : p.getMaterial().getLabel(),
                p.getLogoLevel() == null ? null : p.getLogoLevel().getLabel(),
                p.getSize() == null ? null : p.getSize().getLabel(),
                p.getWearStyle() == null ? null : p.getWearStyle().getLabel(),
                p.getMood() == null ? null : p.getMood().getLabel(),
                p.getImageUrl(),
                p.getStock()
        );
    }
}
