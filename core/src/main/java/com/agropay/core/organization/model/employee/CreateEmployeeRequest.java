package com.agropay.core.organization.model.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateEmployeeRequest(
    @NotBlank(message = "{employee.document-number.not-blank}")
    @Size(max = 8, message = "{employee.document-number.size}")
    String documentNumber,

    @NotNull(message = "{employee.subsidiary-public-id.not-null}")
    UUID subsidiaryPublicId,

    @NotNull(message = "{employee.position-public-id.not-null}")
    UUID positionPublicId,

    UUID retirementConceptPublicId,
    UUID healthInsuranceConceptPublicId

) {}
