package com.agropay.core.payroll.model.calendar;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateWorkingDayRequest(
    @NotNull(message = "{calendar.date.not-null}")
    LocalDate date,

    @NotNull(message = "{calendar.is-working-day.not-null}")
    Boolean isWorkingDay
) {}