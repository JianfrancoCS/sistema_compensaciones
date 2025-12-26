package com.agropay.core.assignment.model.qrroll;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommandQrRollResponse(
        UUID publicId,
        Integer maxQrCodesPerDay,
        LocalDateTime createdAt,
        String createdBy
) {
}
