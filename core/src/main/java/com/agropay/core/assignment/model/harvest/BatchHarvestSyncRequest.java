package com.agropay.core.assignment.model.harvest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchHarvestSyncRequest(
        @NotEmpty(message = "{validation.harvest.records.notempty}")
        @Valid
        List<BatchHarvestRecordData> records
) {
}
