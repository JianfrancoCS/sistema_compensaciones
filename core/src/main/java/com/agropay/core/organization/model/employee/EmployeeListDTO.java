package com.agropay.core.organization.model.employee;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeListDTO(
    UUID publicId,
    String documentNumber,
    String names,
    String paternalLastname,
    String maternalLastname,
    String subsidiaryName,
    String positionName,
    Boolean isNational,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String photoUrl
) {}
