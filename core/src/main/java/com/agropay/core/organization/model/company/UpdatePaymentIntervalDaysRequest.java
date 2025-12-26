package com.agropay.core.organization.model.company;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdatePaymentIntervalDaysRequest(
        @NotNull(message = "{company.payment-interval-days.not-null}")
        @Positive(message = "{company.payment-interval-days.positive}")
        Integer paymentIntervalDays
) {
}
