package com.agropay.core.hiring.model.contracttemplate;

import java.util.List;
import java.util.UUID;

public record CommandContractTemplateResponse(
    UUID publicId,
    String code,
    String name,
    String templateContent,
    UUID contractTypePublicId,
    UUID statePublicId,
    List<ContractTemplateVariableDTO> variables
) {}
