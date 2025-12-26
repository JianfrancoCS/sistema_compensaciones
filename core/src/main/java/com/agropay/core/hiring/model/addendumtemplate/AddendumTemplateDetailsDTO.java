package com.agropay.core.hiring.model.addendumtemplate;

import java.util.List;
import java.util.UUID;

public record AddendumTemplateDetailsDTO(
    UUID publicId,
    String code,
    String name,
    String templateContent,
    UUID addendumTypePublicId,
    String addendumTypeName,
    UUID statePublicId,
    List<AddendumTemplateVariableDTO> variables
) {}