package com.agropay.core.auth.model.element;

import java.time.LocalDateTime;
import java.util.UUID;

public record ElementDetailsDTO(
        UUID publicId,
        String name,
        String displayName,
        String route,
        String icon,
        String iconUrl,
        ContainerInfo container,
        Integer orderIndex,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record ContainerInfo(
            UUID publicId,
            String name,
            String displayName
    ) {}
}

