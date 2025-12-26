package com.agropay.core.attendance.model.marking;

import java.time.LocalDateTime;
import java.util.UUID;

public record MarkingResponse(
    UUID publicId,
    String personDocumentNumber,
    String entryType,
    String markingReasonName,
    String subsidiaryName,
    LocalDateTime markedAt
) {
}