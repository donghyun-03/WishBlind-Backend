package com.example.wishBlind.auth.application;

import com.example.wishBlind.auth.domain.OAuthProvider;
import com.example.wishBlind.auth.domain.SocialAccount;
import com.example.wishBlind.auth.domain.TermsAgreement;
import com.example.wishBlind.auth.domain.TermsType;
import com.example.wishBlind.auth.domain.User;
import com.example.wishBlind.auth.domain.UserStatus;
import com.example.wishBlind.auth.oauth.OAuthClientRegistry;
import com.example.wishBlind.auth.oauth.OAuthUserInfo;
import com.example.wishBlind.auth.repository.SocialAccountRepository;
import com.example.wishBlind.auth.repository.TermsAgreementRepository;
import com.example.wishBlind.auth.repository.UserRepository;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** 가입·로그인·소셜 연결·탈퇴. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final OAuthClientRegistry oauthClientRegistry;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository,
                       SocialAccountRepository socialAccountRepository,
                       TermsAgreementRepository termsAgreementRepository,
                       OAuthClientRegistry oauthClientRegistry,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.termsAgreementRepository = termsAgreementRepository;
        this.oauthClientRegistry = oauthClientRegistry;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public TokenPair signup(String email, String rawPassword, String nickname,
                            String phone, List<TermsAgreementCommand> terms) {
        if (userRepository.existsByEmailAndStatus(email, UserStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        requireMandatoryTerms(terms);

        User user = User.ofLocal(email, passwordEncoder.encode(rawPassword), nickname);
        if (phone != null) {
            user.changePhone(phone);
        }
        userRepository.save(user);
        saveTerms(user.getId(), terms);

        return tokenService.issue(user.getId());
    }

    /**
     * 자체 로그인.
     * 실패 사유(이메일 없음 / 비밀번호 틀림)를 구분하지 않는다 — 구분하면
     * 해당 이메일의 가입 여부를 알려주는 계정 열거 취약점이 된다.
     */
    @Transactional
    public TokenPair login(String email, String rawPassword) {
        User user = userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        return tokenService.issue(user.getId());
    }

    /**
     * 소셜 로그인 겸 가입.
     * 이미 연결된 소셜 계정이면 로그인, 처음이면 회원을 만들고 연결한다.
     * 신규 가입 경로에서는 필수 약관 동의가 함께 와야 한다.
     */
    @Transactional
    public TokenPair socialLogin(OAuthProvider provider, String providerAccessToken,
                                 List<TermsAgreementCommand> terms) {
        OAuthUserInfo info = oauthClientRegistry.get(provider).fetch(providerAccessToken);

        Optional<SocialAccount> existing =
                socialAccountRepository.findByProviderAndProviderUserId(provider, info.providerUserId());
        if (existing.isPresent()) {
            User user = findActiveUser(existing.get().getUserId());
            return tokenService.issue(user.getId());
        }

        requireMandatoryTerms(terms);

        User user = User.ofSocial(info.email(), resolveNickname(info));
        userRepository.save(user);
        socialAccountRepository.save(SocialAccount.of(user.getId(), provider, info.providerUserId()));
        saveTerms(user.getId(), terms);

        return tokenService.issue(user.getId());
    }

    /** 로그인 상태에서 소셜 계정을 추가로 연결한다(account linking). */
    @Transactional
    public void linkSocial(Long userId, OAuthProvider provider, String providerAccessToken) {
        User user = findActiveUser(userId);
        OAuthUserInfo info = oauthClientRegistry.get(provider).fetch(providerAccessToken);

        socialAccountRepository.findByProviderAndProviderUserId(provider, info.providerUserId())
                .ifPresent(account -> {
                    throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_LINKED);
                });
        if (socialAccountRepository.existsByUserIdAndProvider(user.getId(), provider)) {
            throw new BusinessException(ErrorCode.SOCIAL_PROVIDER_ALREADY_LINKED);
        }

        socialAccountRepository.save(SocialAccount.of(user.getId(), provider, info.providerUserId()));
    }

    /** 탈퇴. 회원은 soft delete하고 세션은 전부 끊는다. */
    @Transactional
    public void withdraw(Long userId) {
        User user = findActiveUser(userId);
        user.withdraw();
        tokenService.revokeAll(user.getId());
    }

    @Transactional(readOnly = true)
    public User findActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.isWithdrawn()) {
            throw new BusinessException(ErrorCode.WITHDRAWN_USER);
        }
        return user;
    }

    @Transactional(readOnly = true)
    public List<OAuthProvider> findLinkedProviders(Long userId) {
        return socialAccountRepository.findAllByUserId(userId).stream()
                .map(SocialAccount::getProvider)
                .toList();
    }

    private void requireMandatoryTerms(List<TermsAgreementCommand> terms) {
        Set<TermsType> agreed = (terms == null) ? Set.of()
                : terms.stream()
                        .filter(TermsAgreementCommand::agreed)
                        .map(TermsAgreementCommand::termsType)
                        .collect(Collectors.toSet());

        Set<TermsType> required = Arrays.stream(TermsType.values())
                .filter(TermsType::isRequired)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TermsType.class)));

        if (!agreed.containsAll(required)) {
            throw new BusinessException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }

    /** 동의한 항목만 이력으로 남긴다. 미동의는 기록하지 않는다. */
    private void saveTerms(Long userId, List<TermsAgreementCommand> terms) {
        if (terms == null) {
            return;
        }
        List<TermsAgreement> agreements = terms.stream()
                .filter(TermsAgreementCommand::agreed)
                .map(t -> TermsAgreement.of(userId, t.termsType(), t.version()))
                .toList();
        termsAgreementRepository.saveAll(agreements);
    }

    /** 제공자가 닉네임을 안 주는 경우가 있어 대체값을 만든다. */
    private String resolveNickname(OAuthUserInfo info) {
        if (info.nickname() != null && !info.nickname().isBlank()) {
            return info.nickname();
        }
        String suffix = info.providerUserId();
        if (suffix.length() > 6) {
            suffix = suffix.substring(suffix.length() - 6);
        }
        return "user" + suffix;
    }
}
