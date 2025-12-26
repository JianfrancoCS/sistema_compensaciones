package com.agropay.core.hiring.model.addendum;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SignAddendumRequest(
    @NotEmpty String imagesUri
) {}