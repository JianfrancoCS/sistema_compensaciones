package com.agropay.core.payroll.model.calendar;

import java.util.List;

public record AvailablePeriodDTO(
    int year,
    List<MonthInfoDTO> months
) {}