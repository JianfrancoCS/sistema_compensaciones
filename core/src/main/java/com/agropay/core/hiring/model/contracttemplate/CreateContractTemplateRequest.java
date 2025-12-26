package com.agropay.core.hiring.model.contracttemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateContractTemplateRequest(
    @NotBlank
    @Size(max = 100)
    String name,

    @NotBlank
    String templateContent,

    @NotNull
    UUID contractTypePublicId,

    @NotNull
    UUID statePublicId,

    List<ContractTemplateVariableRequest> variables
) {}
