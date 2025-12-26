package com.agropay.core.hiring.model.addendumtemplate;

import java.util.List;
import java.util.UUID;

public record CommandAddendumTemplateResponse(
    UUID id,
    String code,
    String name,
    String templateContent,
    UUID addendumTypePublicId,
    UUID statePublicId,
    String content,
    List<AddendumTemplateVariableDTO> variables
) {}