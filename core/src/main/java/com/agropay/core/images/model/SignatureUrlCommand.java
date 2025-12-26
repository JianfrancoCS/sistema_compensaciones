package com.agropay.core.images.model;

import lombok.Builder;

@Builder
public record SignatureUrlCommand(
        String uploadUrl,
        String apiKey,
        Long timestamp,
        String signature,
        String folder
) {
}
