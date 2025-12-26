package com.agropay.core.payroll.domain.calculator.impl;

import com.agropay.core.payroll.domain.calculator.ConceptCalculator;
import com.agropay.core.payroll.domain.calculator.EmployeePayrollContext;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Base calculator for AFP (pension fund) concepts
 */
@Slf4j
public abstract class AbstractAFPCalculator implements ConceptCalculator {

    @Override
    public BigDecimal calculate(EmployeePayrollContext context) {
        /**
         * Calcula el aporte a AFP (Administradora de Fondos de Pensiones)
         *
         * En Perú, AFP se calcula sobre la "remuneración computable" que incluye:
         * - Sueldo básico
         * - Horas extras
         * - Bonificaciones remunerativas
         * - Otros conceptos de carácter remunerativo
         *
         * La AFP tiene dos componentes:
         * 1. Aporte Obligatorio: ~10% (varía por AFP)
         * 2. Comisión + Seguro: ~2-3% (varía por AFP)
         *
         * El porcentaje total se almacena en configuredConcepts
         * La base de cálculo es totalIncome (ya acumulado en el context)
         */

        BigDecimal percentage = context.getConfiguredConcepts().get(getConceptCode());

        if (percentage == null) {
            log.warn("No percentage configured for {} for employee {}",
                getConceptCode(), context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }

        // Base de cálculo: totalIncome acumulado hasta este punto
        BigDecimal baseAmount = context.getTotalIncome();

        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Calcular AFP como porcentaje de totalIncome
        return baseAmount.multiply(percentage)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}