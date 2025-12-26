package com.agropay.core.organization.model.company;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdatePayrollDeclarationDayRequest(
    @NotNull(message = "{company.payroll-declaration-day.not-null}")
    @Min(value = 1, message = "{company.payroll-declaration-day.min}")
    @Max(value = 31, message = "{company.payroll-declaration-day.max}")
    Byte payrollDeclarationDay
) {
}
