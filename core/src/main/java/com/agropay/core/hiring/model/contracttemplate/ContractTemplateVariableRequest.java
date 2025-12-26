package com.agropay.core.hiring.model.contracttemplate;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ContractTemplateVariableRequest(
    @NotNull UUID variablePublicId,
    @NotNull Boolean isRequired,
    @NotNull Integer displayOrder
) {}
