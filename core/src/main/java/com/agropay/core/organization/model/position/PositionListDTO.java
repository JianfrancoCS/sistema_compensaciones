package com.agropay.core.organization.model.position;

import java.time.LocalDateTime;
import java.util.UUID;

public record PositionListDTO(
        UUID publicId,
        String name,
        boolean requiresManager,
        boolean unique,
        AreaInfo area,
        RequiredManagerPositionInfo requiredManagerPosition,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record AreaInfo(
            String name
    ) {}

    public record RequiredManagerPositionInfo(
            UUID publicId,
            String name
    ) {}
}
