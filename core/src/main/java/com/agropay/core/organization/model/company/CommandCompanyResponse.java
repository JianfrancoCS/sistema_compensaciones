package com.agropay.core.organization.model.company;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommandCompanyResponse(
        UUID publicId,
        String legalName,
        String tradeName,
        String ruc,
        String companyType,
        Integer paymentIntervalDays,
        Integer maxMonthlyWorkingHours,
        Byte payrollDeclarationDay,
        Byte payrollAnticipationDays,
        BigDecimal overtimeRate,
        BigDecimal dailyNormalHours,
        Integer monthCalculationDays,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
