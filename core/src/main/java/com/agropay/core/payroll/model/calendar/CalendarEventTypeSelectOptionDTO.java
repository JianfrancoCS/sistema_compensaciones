package com.agropay.core.payroll.model.calendar;

import java.util.UUID;

public record CalendarEventTypeSelectOptionDTO(
    UUID publicId,
    String name
) {}