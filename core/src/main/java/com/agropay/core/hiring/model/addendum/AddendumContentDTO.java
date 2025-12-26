package com.agropay.core.hiring.model.addendum;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddendumContentDTO(
    UUID publicId,
    String addendumNumber,
    String mergedContent,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
