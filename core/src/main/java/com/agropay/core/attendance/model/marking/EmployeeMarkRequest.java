package com.agropay.core.attendance.model.marking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record EmployeeMarkRequest(
    @NotBlank(message = "{validation.marking.document-number.notblank}")
    @Size(max = 15, message = "{validation.marking.document-number.size}")
    String personDocumentNumber,

    @NotNull(message = "{validation.marking.subsidiary-id.notnull}")
    UUID subsidiaryPublicId,

    @NotNull(message = "{validation.marking.marking-reason-id.notnull}")
    UUID markingReasonPublicId,

    @NotNull(message = "{validation.marking.is-entry.notnull}")
    Boolean isEntry
) {
}