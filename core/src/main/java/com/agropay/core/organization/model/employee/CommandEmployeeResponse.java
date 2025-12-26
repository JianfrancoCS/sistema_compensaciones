package com.agropay.core.organization.model.employee;

import java.util.UUID;

public record CommandEmployeeResponse(
    UUID publicId,
    String documentNumber,
    String names,
    String paternalLastname,
    String maternalLastname,
    UUID subsidiaryPublicId,
    UUID positionPublicId,
    UUID managerPublicId
) {}
