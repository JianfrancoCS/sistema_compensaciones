package com.agropay.core.organization.model.employee;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateEmployeeSubsidiaryRequest(
        @NotNull(message = "El ID de la nueva sucursal no puede ser nulo.")
        UUID newSubsidiaryPublicId
) {
}
