package com.agropay.core.payroll.model.payroll;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for creating a new Payroll instance.
 */
public record CreatePayrollRequest(
    @NotNull(message = "Subsidiary public ID cannot be null.")
    UUID subsidiaryPublicId,

    @NotNull(message = "Payroll period public ID cannot be null.")
    UUID payrollPeriodPublicId
) {}
