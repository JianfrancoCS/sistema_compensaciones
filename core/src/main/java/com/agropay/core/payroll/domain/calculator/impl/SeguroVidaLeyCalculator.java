package com.agropay.core.payroll.domain.calculator.impl;

import com.agropay.core.payroll.domain.calculator.ConceptCalculator;
import com.agropay.core.payroll.domain.calculator.EmployeePayrollContext;
import com.agropay.core.payroll.domain.enums.ConceptCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculator for Seguro de Vida Ley (employer life insurance contribution)
 */
@Slf4j
@Component
public class SeguroVidaLeyCalculator implements ConceptCalculator {

    @Override
    public BigDecimal calculate(EmployeePayrollContext context) {
        /**
         * Calcula el aporte a Seguro de Vida Ley
         *
         * En Perú, Seguro de Vida Ley es un aporte DEL EMPLEADOR (no del empleado)
         * Se calcula sobre la remuneración mensual del trabajador
         * El porcentaje es fijo: 0.51% de la remuneración (según normativa peruana 2025)
         *
         * Este concepto NO se descuenta al trabajador, pero se registra
         * como costo del empleador en la planilla
         *
         * Base de cálculo: totalIncome acumulado hasta este punto
         */

        BigDecimal percentage = context.getConfiguredConcepts().get(ConceptCode.SEGURO_VIDA_LEY);

        if (percentage == null) {
            log.warn("No percentage configured for SEGURO_VIDA_LEY for employee {}", context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }

        // Base de cálculo: totalIncome acumulado hasta este punto
        BigDecimal baseAmount = context.getTotalIncome();

        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Calcular Seguro de Vida Ley como porcentaje de totalIncome
        return baseAmount.multiply(percentage)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    @Override
    public ConceptCode getConceptCode() {
        return ConceptCode.SEGURO_VIDA_LEY;
    }
}

