package com.agropay.core.organization.model.documenttype;

import java.util.UUID;

public record DocumentTypeSelectOptionDTO(
    UUID publicId,
    String code,
    String name,
    Integer length
) {
}