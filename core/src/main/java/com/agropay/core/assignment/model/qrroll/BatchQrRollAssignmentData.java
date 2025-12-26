package com.agropay.core.assignment.model.qrroll;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BatchQrRollAssignmentData(
        @NotNull(message = "{validation.qr-roll.qr-code-public-id.notnull}")
        UUID qrCodePublicId,

        @NotBlank(message = "{validation.qr-roll.employee-document.notblank}")
        String employeeDocumentNumber
) {
}