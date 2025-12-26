package com.agropay.core.auth.model.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserListDTO(
        UUID publicId,
        String username,
        String employeeId,
        UUID positionId,
        String positionName,
        Boolean isActive,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

