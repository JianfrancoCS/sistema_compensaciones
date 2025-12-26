package com.agropay.core.dashboard.model;

public record PayrollsByPeriodDTO(
        String period,
        Long count,
        Double totalAmount
) {
}

