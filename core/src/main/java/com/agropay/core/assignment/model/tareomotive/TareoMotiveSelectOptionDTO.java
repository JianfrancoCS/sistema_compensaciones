package com.agropay.core.assignment.model.tareomotive;

import java.util.UUID;

public record TareoMotiveSelectOptionDTO(
        UUID publicId,
        String name,
        Boolean isPaid
) {
}