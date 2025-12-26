package com.agropay.core.organization.model.employee;

import java.util.UUID;

public record EmployeeSearchResponse(
        String documentNumber,
        String fullName,
        String position,
        UUID subsidiaryId,
        String subsidiaryName
) {
}