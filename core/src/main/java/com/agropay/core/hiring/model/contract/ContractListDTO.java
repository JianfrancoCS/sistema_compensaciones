package com.agropay.core.hiring.model.contract;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ContractListDTO(
    UUID publicId,
    String contractNumber,
    String personDocumentNumber,
    String contractTypeName,
    LocalDate startDate,
    LocalDate endDate,
    String stateName,
    boolean isSigned,
    boolean hasImages,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
