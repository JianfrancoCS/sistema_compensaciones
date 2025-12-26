package com.agropay.core.assignment.model.lote;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LoteListDTO(
        UUID publicId,
        String name,
        BigDecimal hectareage,
        String subsidiaryName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}