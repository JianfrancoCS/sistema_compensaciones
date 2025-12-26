package com.agropay.core.hiring.model.contracttype;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContractTypeListDTO(
    UUID publicId,
    String code,
    String name,
    Boolean isIndefinite,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
