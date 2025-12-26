package com.agropay.core.assignment.model.tareo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record BatchEmployeeData(
        @NotBlank(message = "{validation.tareo.employee-document-number.notblank}")
        String documentNumber,

        @NotNull(message = "{validation.tareo.entry-time.notnull}")
        LocalTime entryTime,

        UUID entryMotivePublicId,

        LocalTime exitTime,

        UUID exitMotivePublicId
) {
}