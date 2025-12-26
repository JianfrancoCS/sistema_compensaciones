package com.agropay.core.payroll.model.payroll;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Standard response DTO for payroll creation and update operations.
 */
public record CommandPayrollResponse(
    UUID publicId,
    String code,
    String stateName,
    Integer totalEmployees,
    Long tareosToProcess,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
