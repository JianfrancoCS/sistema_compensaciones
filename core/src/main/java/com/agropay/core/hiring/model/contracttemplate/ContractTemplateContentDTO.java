package com.agropay.core.hiring.model.contracttemplate;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContractTemplateContentDTO(
    UUID publicId,
    String code,
    String name,
    String templateContent,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}