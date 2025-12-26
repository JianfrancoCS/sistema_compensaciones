package com.agropay.core.organization.model.area;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAreaRequest(
        @NotBlank(message = "{area.name.not-blank}")
        @Size(max = 100, message = "{area.name.size}")
        String name
) {
}
