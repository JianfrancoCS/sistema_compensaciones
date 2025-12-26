package com.agropay.core.organization.model.location;

import java.util.UUID;

public record ProvinceResponse(
        UUID publicId,
        String name
) {
}
