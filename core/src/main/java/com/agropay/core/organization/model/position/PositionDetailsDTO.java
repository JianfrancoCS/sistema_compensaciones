package com.agropay.core.organization.model.position;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PositionDetailsDTO(
        UUID publicId,
        String name,
        AreaInfo area,
        BigDecimal salary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long employeeCount
) {
    public record AreaInfo(
            String name
    ) {}

    public PositionDetailsDTO(UUID publicId, String name, String areaName, BigDecimal salary, LocalDateTime createdAt, LocalDateTime updatedAt, long employeeCount) {
        this(publicId, name, new AreaInfo(areaName), salary, createdAt, updatedAt, Long.valueOf(employeeCount));
    }
}
