package com.agropay.core.hiring.model.contracttemplate;

import java.util.UUID;

public record ContractTemplateDetailsDTO(
    UUID publicId,
    String name,
    String templateContent,
    UUID contractTypePublicId,
    String contractTypeName,
    Boolean isActive
) {}
