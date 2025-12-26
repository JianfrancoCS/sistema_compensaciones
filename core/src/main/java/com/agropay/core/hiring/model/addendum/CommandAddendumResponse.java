package com.agropay.core.hiring.model.addendum;

import java.util.List;
import java.util.UUID;

public record CommandAddendumResponse(
    UUID id,
    String addendumNumber,
    UUID contractPublicId,
    UUID addendumTypePublicId,
    UUID statePublicId,
    UUID templatePublicId,
    String content,
    List<AddendumVariableValuePayload> variables
) {}