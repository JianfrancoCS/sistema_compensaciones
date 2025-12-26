package com.agropay.core.assignment.model.laborunit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLaborUnitRequest(
        @NotBlank(message = "{laborunit.name.notblank}")
        @Size(max = 50, message = "{laborunit.name.size}")
        String name,

        @NotBlank(message = "{laborunit.abbreviation.notblank}")
        @Size(max = 10, message = "{laborunit.abbreviation.size}")
        String abbreviation,

        @Size(max = 200, message = "{laborunit.description.size}")
        String description
) {
}