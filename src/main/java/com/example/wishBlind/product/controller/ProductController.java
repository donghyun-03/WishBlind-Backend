package com.example.wishBlind.product.controller;

import com.example.wishBlind.global.common.ApiResponse;
import com.example.wishBlind.product.dto.ProductResponse;
import com.example.wishBlind.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Product", description = "브랜드 상품 카탈로그 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 목록 조회", description = "카테고리로 필터 가능(선택). 값이 없으면 전체 반환.")
    @GetMapping
    public ApiResponse<List<ProductResponse>> list(
            @RequestParam(required = false) String category) {
        return ApiResponse.success(productService.getList(category));
    }

    @Operation(summary = "상품 단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable Long id) {
        return ApiResponse.success(productService.get(id));
    }
}
