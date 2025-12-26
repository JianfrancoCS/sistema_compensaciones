package com.agropay.core.assignment.model.lote;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateLoteRequest(
        @NotBlank(message = "{lote.name.notblank}")
        @Size(max = 100, message = "{lote.name.size}")
        String name,

        @DecimalMin(value = "0.0", inclusive = false, message = "{lote.hectareage.min}")
        BigDecimal hectareage,

        @NotNull(message = "{lote.subsidiary-id.notnull}")
        UUID subsidiaryPublicId
) {
}