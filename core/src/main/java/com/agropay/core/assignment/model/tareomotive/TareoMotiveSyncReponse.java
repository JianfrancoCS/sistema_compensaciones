package com.agropay.core.assignment.model.tareomotive;

import java.util.UUID;

public record TareoMotiveSyncReponse(
        UUID publicId,
        String name,
        String description,
        Boolean isPaid
) {

}
