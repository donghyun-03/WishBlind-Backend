package com.example.wishBlind.user.controller;

import com.example.wishBlind.global.common.ApiResponse;
import com.example.wishBlind.user.dto.UserCreateRequest;
import com.example.wishBlind.user.dto.UserResponse;
import com.example.wishBlind.user.dto.UserUpdateRequest;
import com.example.wishBlind.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "마이페이지 API (프로필 · 알림 설정). 로그인/인증은 후순위")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 생성", description = "인증 도입 전 임시 가입(테스트용).")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success(userService.create(request));
    }

    @Operation(summary = "마이페이지 조회", description = "프로필 + 알림 설정 조회.")
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> get(@PathVariable Long id) {
        return ApiResponse.success(userService.get(id));
    }

    @Operation(summary = "정보 수정 완료", description = "닉네임·이메일·비밀번호·프로필 이미지 + 알림 설정(ON/OFF)을 한 번에 저장.")
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id,
                                            @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.success(userService.update(id, request));
    }
}
