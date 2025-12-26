package com.agropay.core.hiring.model.addendum;

import jakarta.validation.constraints.NotBlank;

public record AddendumVariableValuePayload(
    @NotBlank String code,
    @NotBlank String value
) {}