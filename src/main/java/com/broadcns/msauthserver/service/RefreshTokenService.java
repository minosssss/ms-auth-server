package com.broadcns.msauthserver.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RefreshTokenService {

    private final ConcurrentHashMap<String, String> refreshTokenStore = new ConcurrentHashMap<>();

    public void storeRefreshToken(String email, String refreshToken) {
        refreshTokenStore.put(email, refreshToken);
    }

    public void invalidateRefreshToken(String refreshToken) {
        refreshTokenStore.values().removeIf(token -> token.equals(refreshToken));
    }

    public String getStoredRefreshToken(String email) {
        return refreshTokenStore.get(email);
    }
}
