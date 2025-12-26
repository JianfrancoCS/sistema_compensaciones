package com.agropay.core.hiring.model.contracttemplate;

import com.agropay.core.hiring.model.variable.AssociatedMethodDTO;

import java.util.List;
import java.util.UUID;

public record ContractTemplateVariableWithValidationDTO(
    UUID publicId,
    String code,
    String name,
    String dataType,
    String defaultValue,
    Boolean isRequired,
    Integer displayOrder,
    ValidationInfo validation
) {
    public record ValidationInfo(
        Boolean hasValidation,
        String finalRegex,
        String errorMessage,
        List<AssociatedMethodDTO> appliedMethods
    ) {}
}