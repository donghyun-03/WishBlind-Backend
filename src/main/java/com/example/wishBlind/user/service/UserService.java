package com.example.wishBlind.user.service;

import com.example.wishBlind.auth.domain.User;
import com.example.wishBlind.auth.repository.UserRepository;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import com.example.wishBlind.user.dto.UserResponse;
import com.example.wishBlind.user.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 마이페이지 서비스. 회원 본체는 인증(auth) 파트의 User를 재사용하고,
 * 여기서는 프로필 이미지·알림 설정 등 마이페이지 항목만 다룬다.
 * (가입/로그인/이메일·비번 변경은 auth 파트 담당)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponse get(Long id) {
        return UserResponse.from(findById(id));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findById(id);
        user.updateMyPage(request.nickname(), request.profileImageUrl(),
                request.notifyEnabled(), request.notifyGiftProgress(), request.notifyTasteProgress());
        return UserResponse.from(user);
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
