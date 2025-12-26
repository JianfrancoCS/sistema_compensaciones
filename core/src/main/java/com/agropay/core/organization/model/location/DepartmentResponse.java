package com.agropay.core.organization.model.location;

import java.util.UUID;

public record DepartmentResponse(
        UUID publicId,
        String name
) {
}
