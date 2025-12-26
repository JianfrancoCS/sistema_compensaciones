package com.agropay.core.hiring.model.addendumtype;

import java.util.UUID;

public record AddendumTypeListDTO(
    UUID publicId,
    String code,
    String name,
    String description
) {}