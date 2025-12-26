package com.agropay.core.organization.model.company;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateMaxMonthlyWorkingHoursRequest(
        @NotNull(message = "{company.max-monthly-working-hours.not-null}")
        @Positive(message = "{company.max-monthly-working-hours.positive}")
        Integer maxMonthlyWorkingHours
) {
}
