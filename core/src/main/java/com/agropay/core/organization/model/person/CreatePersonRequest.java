package com.agropay.core.organization.model.person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePersonRequest(
    @NotBlank(message = "{person.document-number.not-blank}")
    @Size(max = 8, message = "{person.document-number.size}")
    String documentNumber,

    @NotBlank(message = "{person.names.not-blank}")
    @Size(max = 100, message = "{person.names.size}")
    String names,

    @NotBlank(message = "{person.paternal-lastname.not-blank}")
    @Size(max = 100, message = "{person.paternal-lastname.size}")
    String paternalLastname,

    @NotBlank(message = "{person.maternal-lastname.not-blank}")
    @Size(max = 100, message = "{person.maternal-lastname.size}")
    String maternalLastname,

    @NotNull(message = "{person.dob.not-null}")
    LocalDate dob

) {}
