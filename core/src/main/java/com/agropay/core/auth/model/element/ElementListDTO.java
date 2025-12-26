package com.agropay.core.auth.model.element;

import java.time.LocalDateTime;
import java.util.UUID;

public record ElementListDTO(
        UUID publicId,
        String name,
        String displayName,
        String route,
        String icon,
        String iconUrl,
        ContainerInfo container,
        Integer orderIndex,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record ContainerInfo(
            UUID publicId,
            String name,
            String displayName
    ) {}
}

