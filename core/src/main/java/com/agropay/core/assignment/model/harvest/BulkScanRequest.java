package com.agropay.core.assignment.model.harvest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.List;

public record BulkScanRequest(
        @NotEmpty(message = "{validation.harvest.scans.notempty}")
        @Valid
        List<ScanItem> scans
) {
    public record ScanItem(
            Long qrCode,
            LocalDateTime scannedAt
    ) {}
}
