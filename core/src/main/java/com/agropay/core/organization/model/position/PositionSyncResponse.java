package com.agropay.core.organization.model.position;

import java.util.UUID;

public record PositionSyncResponse(
        UUID publicId,
        String name
) {
}