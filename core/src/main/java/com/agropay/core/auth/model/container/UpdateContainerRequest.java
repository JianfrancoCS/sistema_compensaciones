package com.agropay.core.auth.model.container;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateContainerRequest(
        @NotBlank(message = "{container.name.not-blank}")
        @Size(max = 100, message = "{container.name.size}")
        String name,

        @NotBlank(message = "{container.display-name.not-blank}")
        @Size(max = 150, message = "{container.display-name.size}")
        String displayName,

        @Size(max = 100, message = "{container.icon.size}")
        String icon,

        @Size(max = 500, message = "{container.icon-url.size}")
        String iconUrl,

        @NotNull(message = "{container.order-index.not-null}")
        Integer orderIndex
) {
}

