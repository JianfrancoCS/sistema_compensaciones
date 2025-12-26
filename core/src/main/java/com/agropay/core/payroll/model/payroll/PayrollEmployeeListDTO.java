package com.agropay.core.payroll.model.payroll;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para listar empleados en una planilla con información resumida
 */
public record PayrollEmployeeListDTO(
    UUID publicId,
    String employeeDocumentNumber,
    String employeeFullName,
    String positionName,
    BigDecimal totalIncome,
    BigDecimal totalDeductions,
    BigDecimal netToPay,
    Short daysWorked
) {}

