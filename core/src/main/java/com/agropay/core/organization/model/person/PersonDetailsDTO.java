package com.agropay.core.organization.model.person;

import java.time.LocalDate;
import java.util.UUID;

public record PersonDetailsDTO(
    String documentNumber,
    String names,
    String paternalLastname,
    String maternalLastname,
    LocalDate dob,
    String gender,
    UUID districtPublicId,
    boolean isNational
) {}
