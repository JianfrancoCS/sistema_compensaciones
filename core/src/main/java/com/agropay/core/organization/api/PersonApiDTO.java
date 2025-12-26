package com.agropay.core.organization.api;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record PersonApiDTO(String documentNumber,
    String names,
    String paternalLastname,
    String maternalLastname,
    LocalDate dob
) {
}
