package com.agropay.core.assignment.model.harvest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record BatchHarvestRecordData(
        @NotBlank(message = "{validation.harvest.temporal-id.notblank}")
        String temporalId,

        @NotNull(message = "{validation.harvest.qr-code-public-id.notnull}")
        UUID qrCodePublicId,

        @NotNull(message = "{validation.harvest.scanned-at.notnull}")
        LocalDateTime scannedAt
) {
}
