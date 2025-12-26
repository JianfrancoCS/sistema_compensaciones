package com.agropay.core.hiring.model.contracttype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateContractTypeRequest(
    @NotBlank
    @Size(max = 100)
    String name,

    Boolean isIndefinite,

    @Size(max = 500)
    String description,

    Integer maxDurationMonths
) {}
