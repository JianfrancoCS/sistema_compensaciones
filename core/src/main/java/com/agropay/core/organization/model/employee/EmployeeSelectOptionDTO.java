package com.agropay.core.organization.model.employee;

import java.util.UUID;

public record EmployeeSelectOptionDTO(
        UUID code,
        String fullName
) {
}
