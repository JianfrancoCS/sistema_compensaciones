package com.agropay.core.hiring.model.contract;

import jakarta.validation.constraints.NotBlank;

public record ContractVariableValuePayload(
        @NotBlank String code,
        @NotBlank String value
) {}
