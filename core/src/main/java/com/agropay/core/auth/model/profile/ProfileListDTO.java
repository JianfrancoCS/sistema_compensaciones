package com.agropay.core.auth.model.profile;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProfileListDTO(
        UUID publicId,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

