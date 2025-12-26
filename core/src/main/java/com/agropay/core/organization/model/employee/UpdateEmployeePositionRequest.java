package com.agropay.core.organization.model.employee;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateEmployeePositionRequest(
        @NotNull(message = "El ID de la nueva posición no puede ser nulo.")
        UUID newPositionPublicId,

        // Opcional: El código del nuevo supervisor, solo si la nueva posición lo requiere.
        UUID newManagerCode
) {
}
