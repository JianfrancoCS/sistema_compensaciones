package com.agropay.core.organization.model.subsidiary;

import java.util.UUID;

public record SubsidiarySelectOptionDTO(
        UUID publicId,
        String name
) {
}
