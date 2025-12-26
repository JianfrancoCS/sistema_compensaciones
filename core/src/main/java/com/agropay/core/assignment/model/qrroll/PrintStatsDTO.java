package com.agropay.core.assignment.model.qrroll;

import java.util.UUID;

public record PrintStatsDTO(
        UUID rollPublicId,
        long printedCount,
        long usedCount,
        long notUsedCount
) {
}
