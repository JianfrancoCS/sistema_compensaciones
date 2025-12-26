package com.agropay.core.payroll.model.concept;

import java.util.UUID;

public record ConceptCategoryOptionDTO(
    UUID publicId,
    String code,
    String name
) {}

