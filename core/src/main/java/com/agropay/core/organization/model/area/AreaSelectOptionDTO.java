package com.agropay.core.organization.model.area;

import java.util.UUID;

public record AreaSelectOptionDTO(
        UUID publicId,
        String name
) {
}
