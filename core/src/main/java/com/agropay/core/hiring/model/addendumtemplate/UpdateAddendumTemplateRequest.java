package com.agropay.core.hiring.model.addendumtemplate;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record UpdateAddendumTemplateRequest(
    @Size(max = 100)
    String name,

    String templateContent,

    UUID addendumTypePublicId,

    UUID statePublicId,

    List<AddendumTemplateVariableRequest> variables
) {}