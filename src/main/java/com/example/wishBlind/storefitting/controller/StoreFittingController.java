package com.example.wishBlind.storefitting.controller;

import com.example.wishBlind.global.common.ApiResponse;
import com.example.wishBlind.storefitting.dto.*;
import com.example.wishBlind.storefitting.service.StoreFittingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "StoreFitting", description = "매장 체험 관리 API (직원 페이지)")
@RestController
@RequiredArgsConstructor
public class StoreFittingController {

    private final StoreFittingService storeFittingService;

    @Operation(summary = "체험 예약 생성", description = "선물 세션에 대한 오프라인 매장 체험 예약을 만든다.")
    @PostMapping("/api/fittings")
    public ApiResponse<FittingResponse> create(@Valid @RequestBody FittingCreateRequest request) {
        return ApiResponse.success(storeFittingService.create(request));
    }

    @Operation(summary = "매장 체험 예약 목록", description = "날짜별 예약 목록(기본: 오늘). 상태 배지 포함.")
    @GetMapping("/api/staff/fittings")
    public ApiResponse<List<FittingListResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(storeFittingService.listByDate(date));
    }

    @Operation(summary = "체험 예약 상세", description = "예약 정보 + AI 추천 후보 + 체험 항목 반환.")
    @GetMapping("/api/staff/fittings/{id}")
    public ApiResponse<FittingDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(storeFittingService.getDetail(id));
    }

    @Operation(summary = "체험 시작", description = "상태를 '체험 중'으로 전환.")
    @PostMapping("/api/staff/fittings/{id}/start")
    public ApiResponse<FittingResponse> start(@PathVariable Long id) {
        return ApiResponse.success(storeFittingService.start(id));
    }

    @Operation(summary = "체험 결과 저장", description = "진행 4스텝 결과를 저장하고 상태를 '체험 완료'로 전환. AI 추천 보정에 활용.")
    @PostMapping("/api/staff/fittings/{id}/result")
    public ApiResponse<FittingDetailResponse> submitResult(@PathVariable Long id,
                                                           @Valid @RequestBody FittingResultRequest request) {
        return ApiResponse.success(storeFittingService.submitResult(id, request));
    }
}
