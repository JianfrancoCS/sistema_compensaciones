package com.agropay.core.payroll.domain.calculator.impl;

import com.agropay.core.payroll.domain.calculator.ConceptCalculator;
import com.agropay.core.payroll.domain.calculator.EmployeePayrollContext;
import com.agropay.core.payroll.domain.enums.ConceptCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculator for ESSALUD (employer health contribution)
 */
@Slf4j
@Component
public class ESSALUDCalculator implements ConceptCalculator {

    @Override
    public BigDecimal calculate(EmployeePayrollContext context) {
        /**
         * Calcula el aporte a ESSALUD (Seguro Social de Salud)
         *
         * En Perú, ESSALUD es un aporte DEL EMPLEADOR (no del empleado)
         * Se calcula sobre la remuneración mensual del trabajador
         * El porcentaje es fijo: 6% de la remuneración (según normativa peruana 2025)
         *
         * Este concepto NO se descuenta al trabajador, pero se registra
         * como costo del empleador en la planilla
         *
         * Base de cálculo: totalIncome acumulado hasta este punto
         */

        BigDecimal percentage = context.getConfiguredConcepts().get(ConceptCode.ESSALUD);

        if (percentage == null) {
            log.warn("No percentage configured for ESSALUD for employee {}", context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }

        // Base de cálculo: totalIncome acumulado hasta este punto
        BigDecimal baseAmount = context.getTotalIncome();

        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Calcular ESSALUD como porcentaje de totalIncome
        return baseAmount.multiply(percentage)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    @Override
    public ConceptCode getConceptCode() {
        return ConceptCode.ESSALUD;
    }
}