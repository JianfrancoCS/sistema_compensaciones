package com.agropay.core.payroll.model.period;

import java.time.LocalDate;
import java.util.UUID;

public record CommandPayrollPeriodResponse(
    UUID publicId,
    short year,
    byte month,
    LocalDate periodStart,
    LocalDate periodEnd,
    LocalDate declarationDate
) {}
