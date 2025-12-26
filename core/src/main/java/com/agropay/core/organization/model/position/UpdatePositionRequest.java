package com.agropay.core.organization.model.position;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdatePositionRequest(
        @NotBlank(message = "{position.name.not-blank}")
        @Size(max = 100, message = "{position.name.size}")
        String name,

        @NotNull(message = "{position.area-public-id.not-null}")
        UUID areaPublicId,

        @NotNull(message = "{position.salary.not-null}")
        @DecimalMin(value = "0.01", message = "{position.salary.min}")
        BigDecimal salary,

        @NotNull(message = "{position.requires-manager.not-null}")
        Boolean requiresManager,

        @NotNull(message = "{position.unique.not-null}")
        Boolean unique,

        UUID requiredManagerPositionPublicId
) {
}
