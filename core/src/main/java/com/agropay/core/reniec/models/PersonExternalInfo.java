package com.agropay.core.reniec.models;

import lombok.Builder;

@Builder
public record PersonExternalInfo(
    String dni,
    String paternalLastname,
    String maternalLastname,
    String fullName,
    String gender,
    String birthDate
) {}