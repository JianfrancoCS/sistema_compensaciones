package com.agropay.core.organization.model.subsidiary;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubsidiaryDetailsDTO(
        UUID publicId,
        String name,
        UUID districtId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long employeeCount
) {
    public SubsidiaryDetailsDTO(UUID publicId, String name, UUID districtId, LocalDateTime createdAt, LocalDateTime updatedAt, long employeeCount) {
        this(publicId, name, districtId, createdAt, updatedAt, Long.valueOf(employeeCount));
    }
}
