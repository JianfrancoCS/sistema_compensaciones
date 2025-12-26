package com.agropay.core.auth.model.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "{profile.name.not-blank}")
        @Size(max = 100, message = "{profile.name.size}")
        String name,

        @Size(max = 500, message = "{profile.description.size}")
        String description
) {
}

