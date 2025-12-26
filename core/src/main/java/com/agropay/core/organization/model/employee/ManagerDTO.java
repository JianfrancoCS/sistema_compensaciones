package com.agropay.core.organization.model.employee;

import java.util.UUID;

public record ManagerDTO(
    UUID code,
    String fullName
) {}
