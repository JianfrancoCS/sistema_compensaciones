package com.agropay.core.assignment.model.qrroll;

import jakarta.validation.constraints.Min;

public record UpdateQrRollRequest(
        @Min(value = 1, message = "{qr-roll.max-qr-codes-per-day.min}")
        Integer maxQrCodesPerDay
) {
}