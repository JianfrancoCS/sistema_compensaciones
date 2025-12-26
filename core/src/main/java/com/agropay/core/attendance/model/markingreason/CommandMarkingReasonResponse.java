package com.agropay.core.attendance.model.markingreason;

import java.util.UUID;

public record CommandMarkingReasonResponse(
    UUID publicId,
    String code,
    String name
) {
}