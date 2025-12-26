package com.agropay.core.assignment.model.tareo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchTareoSyncRequest(
        @NotEmpty(message = "{validation.tareo.tareos.notempty}")
        @Valid
        List<BatchTareoData> tareos
) {
}