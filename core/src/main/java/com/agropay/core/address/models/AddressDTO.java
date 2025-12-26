package com.agropay.core.address.models;

import jakarta.validation.constraints.NotBlank;

public record AddressDTO(
        @NotBlank(message = "{address.longitude.not-blank}")
        String longitude,

        @NotBlank(message = "{address.latitude.not-blank}")
        String latitude
) {
}