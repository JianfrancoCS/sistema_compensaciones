package com.agropay.core.assignment.model.qrroll;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BatchGenerateQrCodesRequest(
        @NotNull(message = "{validation.qr-roll.rolls-needed.notnull}")
        @Min(value = 1, message = "{validation.qr-roll.rolls-needed.min}")
        Integer rollsNeeded,

        @NotNull(message = "{validation.qr-roll.codes-per-roll.notnull}")
        @Min(value = 1, message = "{validation.qr-roll.codes-per-roll.min}")
        @Max(value = 1000, message = "{validation.qr-roll.codes-per-roll.max}")
        Integer codesPerRoll
) {
}
