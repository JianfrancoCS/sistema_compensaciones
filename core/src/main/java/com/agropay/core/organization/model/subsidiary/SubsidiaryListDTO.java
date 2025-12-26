package com.agropay.core.organization.model.subsidiary;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubsidiaryListDTO(
        UUID publicId,
        String name,
        String districtName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
