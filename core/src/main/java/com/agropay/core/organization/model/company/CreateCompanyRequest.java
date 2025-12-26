package com.agropay.core.organization.model.company;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateCompanyRequest(
        @NotBlank(message = "{company.legal-name.not-blank}")
        @Size(max = 255, message = "{company.legal-name.size}")
        String legalName,

        @NotBlank(message = "{company.trade-name.not-blank}")
        @Size(max = 255, message = "{company.trade-name.size}")
        String tradeName,

        @NotBlank(message = "{company.ruc.not-blank}")
        @Size(min = 11, max = 11, message = "{company.ruc.size}")
        String ruc,

        @NotBlank(message = "{company.company-type.not-blank}")
        @Size(max = 100, message = "{company.company-type.size}")
        String companyType,

        @NotNull(message = "{company.payment-interval-days.not-null}")
        @Positive(message = "{company.payment-interval-days.positive}")
        Integer paymentIntervalDays,

        @Positive(message = "{company.max-monthly-working-hours.positive}")
        Integer maxMonthlyWorkingHours,

        @NotNull(message = "{company.payroll-declaration-day.not-null}")
        @Min(value = 1, message = "{company.payroll-declaration-day.min}")
        @Max(value = 31, message = "{company.payroll-declaration-day.max}")
        Byte payrollDeclarationDay,

        @NotNull(message = "{company.payroll-anticipation-days.not-null}")
        @Min(value = 0, message = "{company.payroll-anticipation-days.min}")
        Byte payrollAnticipationDays,

        @NotNull(message = "{company.overtime-rate.not-null}")
        @DecimalMin(value = "0.0", inclusive = false, message = "{company.overtime-rate.min}")
        @DecimalMax(value = "1.0", inclusive = false, message = "{company.overtime-rate.max}")
        BigDecimal overtimeRate,

        @NotNull(message = "{company.daily-normal-hours.not-null}")
        @DecimalMin(value = "0.0", inclusive = false, message = "{company.daily-normal-hours.positive}")
        BigDecimal dailyNormalHours,

        @NotNull(message = "{company.month-calculation-days.not-null}")
        @Positive(message = "{company.month-calculation-days.positive}")
        Integer monthCalculationDays
) {
}
