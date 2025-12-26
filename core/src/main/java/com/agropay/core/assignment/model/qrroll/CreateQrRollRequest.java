package com.agropay.core.assignment.model.qrroll;

import jakarta.validation.constraints.NotNull;

public record CreateQrRollRequest(
        @NotNull
        Integer maxQrCodesPerDay
) {
}
