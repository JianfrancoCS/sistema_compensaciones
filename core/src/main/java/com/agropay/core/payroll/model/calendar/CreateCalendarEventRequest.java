package com.agropay.core.payroll.model.calendar;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateCalendarEventRequest(
    @NotNull
    LocalDate date,

    @NotNull
    UUID eventTypePublicId,

    String description
) {}
