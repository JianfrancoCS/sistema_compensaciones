package com.agropay.core.payroll.model.concept;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateConceptRequest(
    @NotBlank(message = "{concept.code.not-blank}")
    @Size(max = 20, message = "{concept.code.size}")
    String code,
    
    @NotBlank(message = "{concept.name.not-blank}")
    @Size(max = 100, message = "{concept.name.size}")
    String name,
    
    @Size(max = 255, message = "{concept.description.size}")
    String description,
    
    @NotNull(message = "{concept.category.not-null}")
    UUID categoryPublicId,
    
    BigDecimal value,
    
    @NotNull(message = "{concept.calculationPriority.not-null}")
    Short calculationPriority
) {}

