package com.agropay.core.hiring.model.contracttype;

import java.util.UUID;

public record ContractTypeSelectOptionDTO(
    UUID publicId,
    String name
) {}
