package com.agropay.core.payroll.model.config;

import java.util.UUID;

/**
 * DTO to represent a concept and its assignment status for a specific payroll.
 */
public record PayrollConceptAssignmentDTO(
    UUID conceptPublicId,
    String name,
    String description,
    String category,
    boolean isAssigned
) {}
