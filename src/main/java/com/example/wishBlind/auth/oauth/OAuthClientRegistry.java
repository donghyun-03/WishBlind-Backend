package com.example.wishBlind.auth.oauth;

import com.example.wishBlind.auth.domain.OAuthProvider;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** provider → OAuthClient 조회. */
@Component
public class OAuthClientRegistry {

    private final Map<OAuthProvider, OAuthClient> clients;

    public OAuthClientRegistry(List<OAuthClient> clients) {
        this.clients = clients.stream()
                .collect(Collectors.toMap(OAuthClient::provider, Function.identity()));
    }

    public OAuthClient get(OAuthProvider provider) {
        OAuthClient client = clients.get(provider);
        if (client == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 소셜 제공자입니다: " + provider);
        }
        return client;
    }
}
