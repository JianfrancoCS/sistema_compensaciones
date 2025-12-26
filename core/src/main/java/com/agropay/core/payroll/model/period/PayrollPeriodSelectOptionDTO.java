package com.agropay.core.payroll.model.period;


import java.util.UUID;

/**
 * DTO for payroll period select options, including a flag if it's assigned to a payroll.
 */
public record PayrollPeriodSelectOptionDTO(
    UUID publicId,
    String name,
    boolean isAssignedToPayroll
) {}