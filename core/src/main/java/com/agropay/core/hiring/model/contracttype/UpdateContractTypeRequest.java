package com.agropay.core.hiring.model.contracttype;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record UpdateContractTypeRequest(
    @NotBlank
    @Size(max = 100)
    String name,

    Boolean isIndefinite,

    @Size(max = 500)
    String description,

    Integer maxDurationMonths
) {}
