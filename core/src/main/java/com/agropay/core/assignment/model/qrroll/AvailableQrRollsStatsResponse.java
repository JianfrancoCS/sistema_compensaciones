package com.agropay.core.assignment.model.qrroll;

public record AvailableQrRollsStatsResponse(
        int totalAvailable,
        int totalInUse
) {
}