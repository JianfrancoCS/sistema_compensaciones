package com.agropay.core.payroll.model.payroll;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PayrollSummaryDTO(
    UUID publicId,
    String code,
    String subsidiaryName,
    UUID subsidiaryPublicId,
    Integer year,
    Short month,
    String periodLabel,
    Long totalEmployees,
    BigDecimal totalIncome,
    BigDecimal totalDeductions,
    BigDecimal totalEmployerContributions,
    BigDecimal totalNet,
    BigDecimal totalHealth, // Total en salud
    BigDecimal totalRetirement, // Total en jubilación
    BigDecimal totalRemuneration, // Total en remuneración
    BigDecimal totalBonus, // Total en bonos
    List<ConceptSummary> incomeConcepts,
    List<ConceptSummary> deductionConcepts,
    List<ConceptSummary> employerContributionConcepts
) {
    public record ConceptSummary(
        String conceptCode,
        String conceptName,
        BigDecimal totalAmount,
        String category
    ) {}
}

