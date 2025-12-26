package com.agropay.core.assignment.model.harvest;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RegisterScanRequest(
        @NotNull(message = "{validation.harvest.qr-code.notnull}")
        Long qrCode,

        @NotNull(message = "{validation.harvest.scanned-at.notnull}")
        LocalDateTime scannedAt
) {
}
