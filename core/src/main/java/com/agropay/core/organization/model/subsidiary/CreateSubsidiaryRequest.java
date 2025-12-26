package com.agropay.core.organization.model.subsidiary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateSubsidiaryRequest(
        @NotBlank(message = "{subsidiary.name.not-blank}")
        @Size(max = 100, message = "{subsidiary.name.size}")
        String name,

        @NotNull(message = "{subsidiary.district.not-null}")
        UUID districtPublicId
) {
}
