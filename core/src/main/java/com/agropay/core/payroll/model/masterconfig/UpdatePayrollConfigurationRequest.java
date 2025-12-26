package com.agropay.core.payroll.model.masterconfig;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * DTO for updating a master payroll configuration.
 */
public record UpdatePayrollConfigurationRequest(
    @NotNull(message = "Concept public IDs list cannot be null.")
    List<UUID> conceptsPublicIds
) {}
