package com.agropay.core.organization.model.employee;

import java.util.UUID;

public record EmployeeCacheResponse(
        String documentNumber,
        String names,
        String paternalLastname,
        String maternalLastname,
        UUID subsidiaryId,
        UUID positionId
) {
}