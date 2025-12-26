package com.agropay.core.organization.model.subsidiary;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubsidiaryDTO(
        UUID uuid,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
