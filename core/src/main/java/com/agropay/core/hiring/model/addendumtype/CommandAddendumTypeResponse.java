package com.agropay.core.hiring.model.addendumtype;

import java.util.UUID;

public record CommandAddendumTypeResponse(
    UUID publicId,
    String code,
    String name,
    String description,
    UUID contractTypePublicId
) {}