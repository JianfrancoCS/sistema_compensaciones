package com.agropay.core.hiring.model.addendumtemplate;

import java.util.UUID;

public record AddendumTemplateVariableDTO(
    UUID publicId,
    String code,
    String name
) {}