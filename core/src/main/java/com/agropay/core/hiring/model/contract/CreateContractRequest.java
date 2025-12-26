package com.agropay.core.hiring.model.contract;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateContractRequest(
    @NotBlank String personDocumentNumber,
    @NotNull UUID subsidiaryPublicId,
    @NotNull UUID positionPublicId,
    @NotNull UUID templatePublicId,
    @NotNull UUID contractTypePublicId,

    @FutureOrPresent LocalDate endDate,

    @Valid List<ContractVariableValuePayload> variables,

    UUID retirementConceptPublicId,
    UUID healthInsuranceConceptPublicId

) {}
