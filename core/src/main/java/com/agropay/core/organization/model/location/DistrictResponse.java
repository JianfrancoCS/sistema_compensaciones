package com.agropay.core.organization.model.location;

import java.util.UUID;

public record DistrictResponse(
        UUID publicId,
        String name,
        String ubigeoInei,
        String ubigeoReniec
) {
}
