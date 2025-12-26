package com.agropay.core.hiring.model.addendum;

import java.util.List;
import java.util.UUID;

public record UpdateAddendumRequest(
    UUID addendumTypePublicId,
    UUID statePublicId,
    UUID templatePublicId,
    List<AddendumVariableValuePayload> variables
) {}