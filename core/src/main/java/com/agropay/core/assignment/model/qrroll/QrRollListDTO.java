package com.agropay.core.assignment.model.qrroll;

import java.time.LocalDateTime;
import java.util.UUID;

public record QrRollListDTO(
        UUID publicId,
        Integer maxQrCodesPerDay,
        Long totalQrCodes,
        Long unprintedQrCodes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
