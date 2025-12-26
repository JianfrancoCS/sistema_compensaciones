package com.agropay.core.organization.model.position;

import java.math.BigDecimal;
import java.util.UUID;

public record CommandPositionResponse(
        UUID publicId,
        String name,
        UUID areaPublicId,
        BigDecimal salary,
        Boolean requiresManager,
        Boolean unique,
        UUID requiredManagerPositionPublicId
) {
}
