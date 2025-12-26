package com.agropay.core.payroll.model.payroll;

import java.math.BigDecimal;

public record PayrollTotalsDTO(
    long totalEmployees,
    BigDecimal totalIncome,
    BigDecimal totalDeductions,
    BigDecimal totalEmployerContributions
) {
}
