package com.agropay.core.payroll.model.masterconfig;

import java.util.UUID;

/**
 * DTO to represent a concept and its assignment status for a specific master payroll configuration.
 */
public record PayrollConfigurationConceptAssignmentDTO(
    UUID conceptPublicId,
    String name,
    boolean isAssigned
) {}
