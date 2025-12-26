package com.agropay.core.organization.model.company;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CompanyDTO(
        UUID publicId,
        String legalName,
        String tradeName,
        String ruc,
        String companyType,
        String logoUrl,
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
