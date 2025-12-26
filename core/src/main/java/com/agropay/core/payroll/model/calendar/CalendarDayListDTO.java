package com.agropay.core.payroll.model.calendar;

import java.time.LocalDate;
import java.util.UUID;

public record CalendarDayListDTO(
    UUID publicId,
    LocalDate date,
    boolean isWorkingDay,
    int eventCount
) {}
