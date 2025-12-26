package com.agropay.core.hiring.model.contract;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContractContentDTO(
    UUID publicId,
    String contractNumber,
    String mergedContent,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
