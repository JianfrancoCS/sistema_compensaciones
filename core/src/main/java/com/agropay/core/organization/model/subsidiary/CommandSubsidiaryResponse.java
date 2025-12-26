package com.agropay.core.organization.model.subsidiary;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommandSubsidiaryResponse(
        UUID publicId,
        String name,
        UUID districtPublicId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
