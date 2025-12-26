package com.agropay.core.organization.model.area;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AreaDetailsDTO(
        UUID publicId,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PositionInfo> positions
) {
    public record PositionInfo(
            UUID publicId,
            String name
    ) {}
}
