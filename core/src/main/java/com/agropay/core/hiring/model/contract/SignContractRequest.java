package com.agropay.core.hiring.model.contract;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SignContractRequest(
    @NotEmpty List<String> imagesUri
) {}
