package com.agropay.core.hiring.model.contracttype;

import java.util.UUID;

public record CommandContractTypeResponse(
    UUID publicId,
    String code,
    String name,
    Boolean isIndefinite,
    String description,
    Integer maxDurationMonths
) {}
