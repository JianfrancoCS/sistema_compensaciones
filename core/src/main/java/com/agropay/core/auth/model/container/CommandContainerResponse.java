package com.agropay.core.auth.model.container;

import java.util.UUID;

public record CommandContainerResponse(
        UUID publicId,
        String name,
        String displayName,
        String icon,
        String iconUrl,
        Integer orderIndex
) {
}

