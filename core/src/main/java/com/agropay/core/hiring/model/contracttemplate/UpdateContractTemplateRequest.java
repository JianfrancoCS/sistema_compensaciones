package com.agropay.core.hiring.model.contracttemplate;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record UpdateContractTemplateRequest(
    @Size(max = 100)
    String name,

    String templateContent,

    UUID contractTypePublicId,

    UUID statePublicId,

    List<ContractTemplateVariableRequest> variables
) {}
