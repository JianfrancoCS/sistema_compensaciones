package com.agropay.core.assignment.model.qrroll;

import java.time.LocalDateTime;
import java.util.UUID;

public record QrCodeDTO(
        UUID publicId,
        Boolean isUsed,
        Boolean isPrinted,
        LocalDateTime createdAt
) {
}
