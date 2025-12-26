package com.agropay.core.auth.model;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String token,
        String refreshToken,
        String tokenType,
        UUID userId,
        String username,
        Long expiresIn,
        Long refreshExpiresIn,
        List<NavigationItemDTO> menu
) {
    public LoginResponse(String token, String refreshToken, UUID userId, String username, Long expiresIn, Long refreshExpiresIn, List<NavigationItemDTO> menu) {
        this(token, refreshToken, "Bearer", userId, username, expiresIn, refreshExpiresIn, menu);
    }
}
