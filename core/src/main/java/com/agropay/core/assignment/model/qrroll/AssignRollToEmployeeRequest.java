package com.agropay.core.assignment.model.qrroll;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignRollToEmployeeRequest(
        @NotNull(message = "{validation.qrroll.roll-id.notnull}")
        UUID rollPublicId,

        @NotBlank(message = "{validation.qrroll.employee-document-number.notblank}")
        String employeeDocumentNumber
) {
}
