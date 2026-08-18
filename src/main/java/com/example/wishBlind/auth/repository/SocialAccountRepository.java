package com.example.wishBlind.auth.repository;

import com.example.wishBlind.auth.domain.OAuthProvider;
import com.example.wishBlind.auth.domain.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    boolean existsByUserIdAndProvider(Long userId, OAuthProvider provider);

    List<SocialAccount> findAllByUserId(Long userId);
}
