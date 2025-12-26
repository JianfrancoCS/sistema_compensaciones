package com.agropay.core.hiring.model.contracttemplate;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContractTemplateListDTO(
    UUID publicId,
    String code,
    String name,
    String contractTypeName,
    String stateName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
