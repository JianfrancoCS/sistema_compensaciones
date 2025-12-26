package com.agropay.core.hiring.model.contracttemplate;

import java.util.UUID;

public record ContractTemplateVariableDTO(
    UUID publicId,
    String code,
    String name,
    String dataType,
    String defaultValue,
    Boolean isRequired,
    Integer displayOrder,
    // Campos de validación dinámica
    String validationRegex,
    String validationErrorMessage,
    Boolean hasValidation
) {}
