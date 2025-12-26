package com.agropay.core.organization.model.employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeDetailsDTO(
    UUID publicId,
    String documentNumber,
    String names,
    String paternalLastname,
    String maternalLastname,
    LocalDate dob,
    String subsidiaryName,
    String positionName,
    String areaName,
    BigDecimal salary,
    ManagerDTO manager,
    String photoUrl
) {}
