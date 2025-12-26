package com.agropay.core.auth.model.container;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContainerListDTO(
        UUID publicId,
        String name,
        String displayName,
        String icon,
        String iconUrl,
        Integer orderIndex,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

