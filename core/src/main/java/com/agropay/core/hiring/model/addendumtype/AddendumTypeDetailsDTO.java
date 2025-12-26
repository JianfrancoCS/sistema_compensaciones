package com.agropay.core.hiring.model.addendumtype;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddendumTypeDetailsDTO(
    UUID publicId,
    String code,
    String name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}