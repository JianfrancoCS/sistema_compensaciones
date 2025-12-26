package com.agropay.core.auth.model.user;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record SyncUserProfilesRequest(
        @NotEmpty(message = "{validation.profiles.ids.not-empty}")
        List<UUID> profileIds
) {}

