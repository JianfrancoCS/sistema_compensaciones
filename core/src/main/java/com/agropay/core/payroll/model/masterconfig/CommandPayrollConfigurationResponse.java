package com.agropay.core.payroll.model.masterconfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for a standard command response for master payroll configurations.
 */
public record CommandPayrollConfigurationResponse(
    UUID publicId,
    String code,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<SimpleConceptDTO> concepts
) {
    public record SimpleConceptDTO(
        UUID publicId,
        String name
    ) {}
}
