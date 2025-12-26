package com.agropay.core.auth.model.profile;

import java.util.UUID;

public record CommandProfileResponse(
        UUID publicId,
        String name,
        String description
) {
}

