package com.agropay.core.dashboard.model;

public record PayrollsByStatusDTO(
        String status,
        Long count,
        Double amount
) {
}

