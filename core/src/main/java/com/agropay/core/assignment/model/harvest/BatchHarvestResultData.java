package com.agropay.core.assignment.model.harvest;

import java.util.UUID;

public record BatchHarvestResultData(
        UUID publicId
) {
    public static BatchHarvestResultData of(UUID publicId) {
        return new BatchHarvestResultData(publicId);
    }
}
