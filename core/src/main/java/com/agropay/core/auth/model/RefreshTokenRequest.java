package com.agropay.core.auth.model;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "{auth.refresh-token.not-blank}")
        String refreshToken
) {
}

