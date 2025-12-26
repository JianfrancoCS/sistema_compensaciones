package com.agropay.core.organization.model.subsidiary;

import java.util.UUID;

public record SubsidiarySyncResponse(
        UUID publicId,
        String name
) {
}