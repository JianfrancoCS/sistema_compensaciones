package com.agropay.core.auth.model.user;

import java.util.UUID;

public record ProfileForAssignmentDTO(
        UUID publicId,
        String name,
        String description,
        boolean isSelected,
        String username
) {}

