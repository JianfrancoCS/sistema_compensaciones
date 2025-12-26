package com.agropay.core.assignment.model.labor;

import java.math.BigDecimal;
import java.util.UUID;

public record LaborSyncResponse(
        UUID publicId,
        String name,
        String description,
        Boolean isPiecework,
        String laborUnitName,
        BigDecimal minTaskRequirement,
        BigDecimal basePrice
) {
}
