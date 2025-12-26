package com.agropay.core.hiring.model.contract;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CancelContractRequest(
        @NotNull(message = "{contract.state.not-null}")
        UUID statePublicId
) {
}