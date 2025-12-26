package com.agropay.core.organization.model.location;

import java.util.UUID;

public record CommandDistrictResponse(
        UUID publicId,
        String name,
        String codeInei,
        String codeReniec
) {
}
