package com.agropay.core.auth.model.profile;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProfileDetailsDTO(
        UUID publicId,
        String name,
        String description,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

