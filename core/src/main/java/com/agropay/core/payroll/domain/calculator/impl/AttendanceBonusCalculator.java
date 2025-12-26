package com.agropay.core.payroll.domain.calculator.impl;

import com.agropay.core.payroll.domain.calculator.ConceptCalculator;
import com.agropay.core.payroll.domain.calculator.EmployeePayrollContext;
import com.agropay.core.payroll.domain.enums.ConceptCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Calculator for attendance bonus
 */
@Slf4j
@Component
public class AttendanceBonusCalculator implements ConceptCalculator {

    @Override
    public BigDecimal calculate(EmployeePayrollContext context) {
        /**
         * Calcula el bono por asistencia perfecta
         *
         * Este bono se otorga a empleados que asistieron TODOS los días laborables del período
         * sin faltas, tardanzas ni permisos
         *
         * Condición: totalHours debe corresponder a trabajar todos los días completos
         *
         * Fórmula: Monto fijo si trabajó todos los días, 0 si faltó algún día
         */

        BigDecimal bonusAmount = context.getConfiguredConcepts().get(ConceptCode.ATTENDANCE_BONUS);

        if (bonusAmount == null) {
            return BigDecimal.ZERO;
        }

        Integer totalWorkingDays = context.getTotalWorkingDays();
        BigDecimal normalHours = context.getNormalHours();
        BigDecimal dailyNormalHours = context.getDailyNormalHours();

        if (totalWorkingDays == null || totalWorkingDays == 0) {
            log.warn("No attendance data for employee {}", context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }

        if (normalHours == null || dailyNormalHours == null) {
            log.warn("Missing hours data for attendance bonus calculation for employee {}",
                context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }

        // Calcular horas esperadas si trabajó todos los días completos
        BigDecimal expectedHours = dailyNormalHours.multiply(new BigDecimal(totalWorkingDays))
            .setScale(2, java.math.RoundingMode.HALF_UP);

        // Otorgar bono solo si trabajó todas las horas esperadas
        if (normalHours.compareTo(expectedHours) >= 0) {
            return bonusAmount;
        }

        return BigDecimal.ZERO;
    }

    @Override
    public ConceptCode getConceptCode() {
        return ConceptCode.ATTENDANCE_BONUS;
    }
}