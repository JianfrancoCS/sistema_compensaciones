package com.agropay.core.hiring.model.addendumtemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateAddendumTemplateRequest(
    @NotBlank
    @Size(max = 100)
    String name,

    @NotBlank
    String templateContent,

    @NotNull
    UUID addendumTypePublicId,

    @NotNull
    UUID statePublicId,

    List<AddendumTemplateVariableRequest> variables
) {}