package com.agropay.core.payroll.model.masterconfig;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record UpdateConceptAssignmentsRequest(
    @NotNull(message = "Concept public IDs list cannot be null.")
    List<UUID> conceptPublicIds
) {}
