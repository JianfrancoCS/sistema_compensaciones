package com.agropay.core.payroll.model.calendar;

import java.util.UUID;

public record CommandCalendarEventResponse(
    UUID publicId,
    String description,
    UUID eventTypePublicId,
    String eventTypeName
) {}
