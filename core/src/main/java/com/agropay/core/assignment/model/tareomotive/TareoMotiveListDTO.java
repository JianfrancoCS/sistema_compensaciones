package com.agropay.core.assignment.model.tareomotive;

import java.time.LocalDateTime;
import java.util.UUID;

public record TareoMotiveListDTO(
        UUID publicId,
        String name,
        String description,
        Boolean isPaid,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}