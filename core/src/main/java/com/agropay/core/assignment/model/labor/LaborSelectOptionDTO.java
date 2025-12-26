package com.agropay.core.assignment.model.labor;

import java.util.UUID;

public record LaborSelectOptionDTO(
        UUID publicId,
        String name,
        Boolean isPiecework
) {
}