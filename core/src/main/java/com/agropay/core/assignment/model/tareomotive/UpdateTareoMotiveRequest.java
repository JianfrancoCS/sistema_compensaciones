package com.agropay.core.assignment.model.tareomotive;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTareoMotiveRequest(
        @NotBlank(message = "{tareomotive.name.notblank}")
        @Size(max = 100, message = "{tareomotive.name.size}")
        String name,

        @Size(max = 500, message = "{tareomotive.description.size}")
        String description,

        @NotNull(message = "{tareomotive.is-paid.notnull}")
        Boolean isPaid
) {
}