package com.agropay.core.hiring.model.addendumtemplate;

import java.util.UUID;

public record AddendumTemplateSelectOptionDTO(
    UUID publicId,
    String code,
    String name
) {}