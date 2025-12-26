package com.agropay.core.assignment.model.qrroll;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GenerateQrCodesRequest(
        @NotNull(message = "{validation.qrroll.quantity.notnull}")
        @Min(value = 1, message = "{validation.qrroll.quantity.min}")
        @Max(value = 1000, message = "{validation.qrroll.quantity.max}")
        Integer quantity
) {
}