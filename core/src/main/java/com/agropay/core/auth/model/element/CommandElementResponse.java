package com.agropay.core.auth.model.element;

import java.util.UUID;

public record CommandElementResponse(
        UUID publicId,
        String name,
        String displayName,
        String route,
        String icon,
        String iconUrl,
        UUID containerPublicId,
        Integer orderIndex
) {
}

