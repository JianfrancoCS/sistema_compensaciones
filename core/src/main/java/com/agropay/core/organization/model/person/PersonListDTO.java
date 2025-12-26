package com.agropay.core.organization.model.person;

import java.time.LocalDateTime;

public record PersonListDTO(
    String documentNumber,
    String names,
    String paternalLastname,
    String maternalLastname,
    String gender,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean isNational
) {}
