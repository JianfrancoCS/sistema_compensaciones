package com.agropay.core.hiring.model.contracttemplate;

import java.util.UUID;

public record ContractTemplateSelectOptionDTO(
    UUID publicId,
    String name
) {}
