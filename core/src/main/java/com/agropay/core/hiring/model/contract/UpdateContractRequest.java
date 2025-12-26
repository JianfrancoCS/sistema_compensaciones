package com.agropay.core.hiring.model.contract;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateContractRequest(
        UUID contractTypePublicId,
        UUID statePublicId,
        UUID subsidiaryPublicId,
        UUID positionPublicId,
        UUID templatePublicId,
        List<ContractVariableValuePayload> variables
) {
}
