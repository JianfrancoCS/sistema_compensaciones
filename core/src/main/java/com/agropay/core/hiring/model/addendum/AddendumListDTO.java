package com.agropay.core.hiring.model.addendum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AddendumListDTO(
    UUID publicId,
    String addendumNumber,
    String contractNumber,
    String addendumTypeName,
    LocalDate startDate,
    String stateName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}