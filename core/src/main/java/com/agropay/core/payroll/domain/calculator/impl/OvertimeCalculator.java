package com.agropay.core.payroll.domain.calculator.impl;

import com.agropay.core.payroll.domain.calculator.ConceptCalculator;
import com.agropay.core.payroll.domain.calculator.EmployeePayrollContext;
import com.agropay.core.payroll.domain.enums.ConceptCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculator for overtime payment
 */
@Slf4j
@Component
public class OvertimeCalculator implements ConceptCalculator {

    @Override
    public BigDecimal calculate(EmployeePayrollContext context) {
        /**
         * Calcula el pago por horas extras
         *
         * En Perú, las horas extras se pagan con recargo:
         * - Primeras 2 horas extras al día (lunes a sábado): +25% (factor 1.25)
         * - Horas extras adicionales y domingos/feriados: +100% (factor 2.00)
         *
         * Fórmula:
         * 1. Valor hora = basicSalary / (monthCalculationDays * dailyNormalHours)
         * 2. Pago HE 25% = valorHora * 1.25 * horasExtras25
         * 3. Pago HE 100% = valorHora * 2.00 * horasExtras100
         * 4. Total = Pago HE 25% + Pago HE 100%
         */

        BigDecimal overtimeHours25 = context.getOvertimeHours25();
        BigDecimal overtimeHours100 = context.getOvertimeHours100();

        BigDecimal basicSalary = context.getBasicSalary();
        BigDecimal dailyNormalHours = context.getDailyNormalHours();
        Integer monthDays = context.getMonthCalculationDays();

        if (basicSalary == null || dailyNormalHours == null || monthDays == null) {
            log.warn("Missing data for overtime calculation for employee {}", context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }

        // Calcular valor hora = basicSalary / (monthDays * dailyHours)
        BigDecimal totalMonthHours = dailyNormalHours.multiply(new BigDecimal(monthDays));
        BigDecimal hourlyRate = basicSalary.divide(totalMonthHours, 4, RoundingMode.HALF_UP);

        BigDecimal total = BigDecimal.ZERO;

        // Recargos por horas extras
        BigDecimal surcharge25 = new BigDecimal("0.25"); // 25% de recargo
        BigDecimal surcharge100 = new BigDecimal("1.00"); // 100% de recargo

        // Horas extras al 25% (primeras 2 horas extras diarias)
        if (overtimeHours25 != null && overtimeHours25.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal multiplier25 = BigDecimal.ONE.add(surcharge25); // 1 + 0.25 = 1.25
            BigDecimal payment25 = hourlyRate.multiply(multiplier25)
                .multiply(overtimeHours25);
            total = total.add(payment25);
        }

        // Horas extras al 100% (horas adicionales + domingos/feriados)
        if (overtimeHours100 != null && overtimeHours100.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal multiplier100 = BigDecimal.ONE.add(surcharge100); // 1 + 1.00 = 2.00
            BigDecimal payment100 = hourlyRate.multiply(multiplier100)
                .multiply(overtimeHours100);
            total = total.add(payment100);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public ConceptCode getConceptCode() {
        return ConceptCode.OVERTIME;
    }
}