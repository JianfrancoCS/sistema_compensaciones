package com.agropay.core.payroll.model.calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CalendarDayDetailDTO(
    UUID publicId,
    LocalDate date,
    Boolean isWorkingDay,
    List<CommandCalendarEventResponse> events
) {}