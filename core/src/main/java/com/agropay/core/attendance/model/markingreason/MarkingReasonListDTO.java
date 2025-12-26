package com.agropay.core.attendance.model.markingreason;

import java.time.LocalDateTime;
import java.util.UUID;

public record MarkingReasonListDTO(
    UUID publicId,
    String code,
    String name,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}