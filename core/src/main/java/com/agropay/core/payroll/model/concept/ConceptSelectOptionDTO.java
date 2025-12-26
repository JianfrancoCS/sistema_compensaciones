package com.agropay.core.payroll.model.concept;

import java.math.BigDecimal;
import java.util.UUID;

public record ConceptSelectOptionDTO(
    UUID publicId,
    String name,
    BigDecimal value,
    String categoryName
) {}