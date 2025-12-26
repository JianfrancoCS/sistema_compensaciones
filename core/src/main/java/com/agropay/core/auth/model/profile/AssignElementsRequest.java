package com.agropay.core.auth.model.profile;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record AssignElementsRequest(
        @NotNull(message = "{profile.elements.not-null}")
        List<UUID> elementPublicIds
) {
}

