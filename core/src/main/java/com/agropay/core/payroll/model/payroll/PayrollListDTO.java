package com.agropay.core.payroll.model.payroll;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for listing payrolls with descriptive fields.
 */
public record PayrollListDTO(
    UUID publicId,
    String code,
    String subsidiaryName,
    String periodName, // e.g., "Marzo 2025"
    String stateName,
    Integer totalEmployees,
    Long processedTareos, // Cantidad de tareos procesados en esta planilla
    Boolean hasPayslips, // Indica si la planilla tiene boletas generadas
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
