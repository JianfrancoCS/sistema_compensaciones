package com.agropay.core.payroll.model.concept;

import java.math.BigDecimal;
import java.util.UUID;

public record ConceptDetailsDTO(
    UUID publicId,
    String code,
    String name,
    String description,
    UUID categoryPublicId,
    String categoryName,
    BigDecimal value,
    Short calculationPriority
) {}

