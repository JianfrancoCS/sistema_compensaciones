package com.agropay.core.organization.model.area;

import java.time.LocalDateTime;
import java.util.UUID;

public record AreaListDTO(
        UUID publicId,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
