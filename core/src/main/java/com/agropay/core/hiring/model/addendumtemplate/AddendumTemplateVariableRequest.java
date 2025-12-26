package com.agropay.core.hiring.model.addendumtemplate;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddendumTemplateVariableRequest(
    @NotNull UUID variablePublicId,
    @NotNull Boolean isRequired,
    @NotNull Integer displayOrder
) {}