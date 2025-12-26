package com.agropay.core.payroll.model.masterconfig;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for listing master payroll configurations.
 */
public record PayrollConfigurationListDTO(
    UUID publicId,
    String code,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
