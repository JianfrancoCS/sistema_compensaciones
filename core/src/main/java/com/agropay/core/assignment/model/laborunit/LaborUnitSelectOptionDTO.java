package com.agropay.core.assignment.model.laborunit;

import java.util.UUID;

public record LaborUnitSelectOptionDTO(
        UUID publicId,
        String name,
        String abbreviation
) {
}