package com.agropay.core.attendance.model.markingreason;

import java.util.UUID;

public record MarkingReasonSelectOptionDTO(
    UUID publicId,
    String name
) {
}