package com.agropay.core.assignment.model.harvest;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommandHarvestRecordResponse(
        UUID publicId,
        Long qrCode,
        LocalDateTime scannedAt
) {
}