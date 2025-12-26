package com.agropay.core.organization.model.person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record UpdatePersonManualRequest(
    @NotBlank(message = "exception.person.names.required")
    String names,

    @NotBlank(message = "exception.person.paternal-lastname.required")
    String paternalLastname,

    @NotBlank(message = "exception.person.maternal-lastname.required")
    String maternalLastname,

    @NotNull(message = "exception.person.date-of-birth.required")
    LocalDate dateOfBirth
) {
}