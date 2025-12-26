package com.agropay.core.payroll.model.config;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * DTO for updating the list of concepts assigned to a payroll.
 */
public record UpdatePayrollConceptsRequest(
    @NotNull(message = "The list of concept public IDs cannot be null.")
    List<UUID> conceptPublicIds
) {}
