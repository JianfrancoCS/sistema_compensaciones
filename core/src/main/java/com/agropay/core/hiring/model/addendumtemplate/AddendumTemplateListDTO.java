package com.agropay.core.hiring.model.addendumtemplate;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddendumTemplateListDTO(
    UUID publicId,
    String code,
    String name,
    String addendumTypeName,
    String stateName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}