package com.agropay.core.payroll.model.concept;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ConceptListDTO(
    UUID publicId,
    String code,
    String name,
    String description,
    String categoryName,
    BigDecimal value,
    Short calculationPriority,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

