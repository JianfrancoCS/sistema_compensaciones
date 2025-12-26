package com.agropay.core.assignment.model.labor;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLaborRequest(
        @NotBlank(message = "{labor.name.notblank}")
        @Size(max = 100, message = "{labor.name.size}")
        String name,

        @Size(max = 500, message = "{labor.description.size}")
        String description,

        @DecimalMin(value = "0.0", inclusive = false, message = "{labor.min-task-requirement.min}")
        BigDecimal minTaskRequirement,

        @NotNull(message = "{labor.labor-unit-id.notnull}")
        UUID laborUnitPublicId,

        @NotNull(message = "{labor.is-piecework.notnull}")
        Boolean isPiecework,

        @DecimalMin(value = "0.0", inclusive = false, message = "{labor.base-price.min}")
        BigDecimal basePrice
) {
}