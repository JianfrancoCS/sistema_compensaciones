package com.agropay.core.dashboard.model;

public record TareosByLaborDTO(
        String laborName,
        Long count,
        Long employeeCount
) {
}

