package com.agropay.core.organization.model.person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateAddressPersonRequest(
    @NotNull
    UUID districtPublicId
) {}
