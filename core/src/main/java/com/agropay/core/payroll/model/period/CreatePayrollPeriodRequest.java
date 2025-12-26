package com.agropay.core.payroll.model.period;

import java.time.LocalDate;

public record CreatePayrollPeriodRequest(
    // If null, the system will create a continuous period based on the last one.
    // If provided, the system will start a new period from this date.
    LocalDate explicitStartDate
) {}
