package com.agropay.core.payroll.domain.calculator;

import com.agropay.core.payroll.domain.enums.ConceptCode;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Context containing all necessary data to calculate payroll concepts for an employee
 */
@Data
@Builder
public class EmployeePayrollContext {

    // Employee information
    private String employeeDocumentNumber;
    private BigDecimal basicSalary;
    private Short retirementConceptId;
    private Short healthInsuranceConceptId;

    // Period information
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private List<LocalDate> workingDays; // Días laborales según calendario de la empresa
    private Integer totalWorkingDays;
    private Integer daysWorked; // Días realmente trabajados (con tareos registrados)
    private Set<LocalDate> workedDays; // Set de fechas con tareos registrados (para verificación rápida)
    private Map<LocalDate, WorkCalendarDayInfo> calendarInfo; // Información del calendario por día (feriados, domingos, etc.)

    // Company configuration
    private BigDecimal overtimeRate;
    private BigDecimal dailyNormalHours;
    private Integer monthCalculationDays;

    // Configured concepts for this payroll with their values
    private Map<ConceptCode, BigDecimal> configuredConcepts;

    // Additional data for calculations
    private Integer numberOfDependents;
    private BigDecimal productivityScore; // Deprecated: usar productivityPerDay en su lugar
    private Map<LocalDate, BigDecimal> productivityPerDay; // Productividad por día (para cálculo de bono día por día)
    private Map<LocalDate, PieceworkDayInfo> pieceworkInfoPerDay; // Información de labor de destajo por día (basePrice, minTaskRequirement, productivityCount)

    // Hours breakdown
    private BigDecimal normalHours;
    private BigDecimal overtimeHours25;
    private BigDecimal overtimeHours100;
    private BigDecimal totalHours;

    // Calculated totals (accumulated during processing)
    @Builder.Default
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalEmployerContributions = BigDecimal.ZERO;
}
