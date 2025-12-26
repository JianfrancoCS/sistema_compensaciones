package com.agropay.core.attendance.model.markingreason;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMarkingReasonRequest(
    @NotBlank(message = "{validation.markingreason.name.notblank}")
    @Size(max = 100, message = "{validation.markingreason.name.size}")
    String name
) {
}