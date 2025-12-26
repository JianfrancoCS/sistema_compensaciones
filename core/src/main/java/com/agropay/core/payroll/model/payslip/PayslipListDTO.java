package com.agropay.core.payroll.model.payslip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for listing payslips with basic information.
 */
public record PayslipListDTO(
    UUID publicId,
    UUID payrollPublicId,
    String payrollCode,
    String employeeDocumentNumber,
    String employeeNames,
    String employeeFullName,
    String subsidiaryName,
    String periodName, // e.g., "Marzo 2025"
    LocalDate periodStart,
    LocalDate periodEnd,
    BigDecimal totalIncome,
    BigDecimal totalDeductions,
    BigDecimal netToPay,
    LocalDateTime createdAt,
    String payslipPdfUrl // URL del PDF almacenado (Cloudinary o similar)
) {}

