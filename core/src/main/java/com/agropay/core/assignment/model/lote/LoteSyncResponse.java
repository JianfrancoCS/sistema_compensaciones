package com.agropay.core.assignment.model.lote;

import java.math.BigDecimal;
import java.util.UUID;

public record LoteSyncResponse(
        UUID id,
        String name,
        BigDecimal hectareage,
        UUID subsidiaryId
) {
}