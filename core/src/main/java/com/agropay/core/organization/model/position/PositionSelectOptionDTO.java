package com.agropay.core.organization.model.position;

import java.util.UUID;

public record PositionSelectOptionDTO(
        UUID publicId,
        String name
) {
}
