package com.agropay.core.assignment.model.qrroll;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchAssignQrRollsRequest(
        @NotEmpty(message = "{validation.qr-roll.assignments.notempty}")
        @Valid
        List<BatchQrRollAssignmentData> assignments
) {
}