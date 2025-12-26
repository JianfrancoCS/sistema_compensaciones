package com.agropay.core.auth.model.element;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateElementRequest(
        @NotBlank(message = "{element.name.not-blank}")
        @Size(max = 100, message = "{element.name.size}")
        String name,

        @NotBlank(message = "{element.display-name.not-blank}")
        @Size(max = 150, message = "{element.display-name.size}")
        String displayName,

        @Size(max = 255, message = "{element.route.size}")
        String route,

        @Size(max = 100, message = "{element.icon.size}")
        String icon,

        @Size(max = 500, message = "{element.icon-url.size}")
        String iconUrl,

        UUID containerPublicId,

        @NotNull(message = "{element.order-index.not-null}")
        Integer orderIndex
) {
}

