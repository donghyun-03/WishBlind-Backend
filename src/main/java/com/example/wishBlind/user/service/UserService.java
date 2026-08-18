package com.example.wishBlind.user.service;

import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import com.example.wishBlind.user.domain.User;
import com.example.wishBlind.user.dto.UserCreateRequest;
import com.example.wishBlind.user.dto.UserResponse;
import com.example.wishBlind.user.dto.UserUpdateRequest;
import com.example.wishBlind.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATE);
        }
        User saved = userRepository.save(User.builder()
                .nickname(request.nickname())
                .email(request.email())
                .password(request.password())
                .build());
        return UserResponse.from(saved);
    }

    public UserResponse get(Long id) {
        return UserResponse.from(findById(id));
    }

    /** 마이페이지 정보 수정(프로필 + 알림 설정). */
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findById(id);
        // 이메일을 다른 사용자가 쓰고 있으면 중복
        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATE);
        }
        user.updateProfile(request.nickname(), request.email(), request.password(), request.profileImageUrl(),
                request.notifyEnabled(), request.notifyGiftProgress(), request.notifyTasteProgress());
        return UserResponse.from(user);
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
