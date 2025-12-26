package com.agropay.core.auth.model.user;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "{validation.user.isActive.not-null}")
        Boolean isActive
) {}

