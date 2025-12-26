package com.agropay.core.organization.model.employee;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeMeResponse(
        UUID code,
        String documentNumber,
        String names,
        String paternalLastname,
        String maternalLastname,
        LocalDate dateOfBirth,
        String gender,
        String positionName,
        UUID subsidiaryId,
        String subsidiaryName,
        String photoUrl
) {
}