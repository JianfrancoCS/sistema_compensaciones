package com.agropay.core.organization.model.employee;


import java.util.UUID;

public record UpdateEmployeeRequest(

    UUID districtPublicId,

    UUID subsidiaryPublicId,

    UUID positionPublicId,

    UUID statePublicId,

    UUID managerCode
) {}
