package com.agropay.core.assignment.model.lote;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommandLoteResponse(
        UUID publicId,
        String name,
        BigDecimal hectareage,
        UUID subsidiaryPublicId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}