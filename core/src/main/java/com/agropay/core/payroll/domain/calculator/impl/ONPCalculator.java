package com.agropay.core.payroll.domain.calculator.impl;

import com.agropay.core.payroll.domain.calculator.ConceptCalculator;
import com.agropay.core.payroll.domain.calculator.EmployeePayrollContext;
import com.agropay.core.payroll.domain.enums.ConceptCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculator for ONP (National Pension System)
 */
@Slf4j
@Component
public class ONPCalculator implements ConceptCalculator {

    @Override
    public BigDecimal calculate(EmployeePayrollContext context) {
        /**
         * Calcula el aporte a ONP (Oficina de Normalización Previsional)
         *
         * En Perú, ONP es el sistema público de pensiones (alternativa a AFP)
         * Se calcula sobre la "remuneración computable" igual que AFP
         * El porcentaje es fijo: 13% de la remuneración computable
         *
         * Base de cálculo: totalIncome acumulado hasta este punto
         */

        BigDecimal percentage = context.getConfiguredConcepts().get(ConceptCode.ONP);

        if (percentage == null) {
            log.warn("No percentage configured for ONP for employee {}", context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }

        // Base de cálculo: totalIncome acumulado hasta este punto
        BigDecimal baseAmount = context.getTotalIncome();

        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Calcular ONP como porcentaje de totalIncome
        return baseAmount.multiply(percentage)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    @Override
    public ConceptCode getConceptCode() {
        return ConceptCode.ONP;
    }
}
