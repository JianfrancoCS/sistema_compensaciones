package com.agropay.core.auth.model.user;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record AssignElementsRequest(
        @NotNull(message = "elementPublicIds cannot be null")
        List<UUID> elementPublicIds
) {}

