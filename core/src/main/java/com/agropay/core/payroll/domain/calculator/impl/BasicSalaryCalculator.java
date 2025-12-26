package com.agropay.core.payroll.domain.calculator.impl;

import com.agropay.core.payroll.domain.calculator.ConceptCalculator;
import com.agropay.core.payroll.domain.calculator.EmployeePayrollContext;
import com.agropay.core.payroll.domain.calculator.WorkCalendarDayInfo;
import com.agropay.core.payroll.domain.enums.ConceptCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

/**
 * Calculator for basic salary concept
 */
@Slf4j
@Component
public class BasicSalaryCalculator implements ConceptCalculator {

    @Override
    public BigDecimal calculate(EmployeePayrollContext context) {
        /**
         * Calcula el sueldo básico del empleado día por día con multiplicadores.
         * 
         * IMPORTANTE: El salario se calcula día por día para evitar pagar el salario completo
         * si el empleado faltó días. Si faltó 3 días, solo se paga por los días trabajados.
         * 
         * Reglas de cálculo por día:
         * - Día normal trabajado: básico diario x1
         * - Día feriado trabajado: básico diario x2 (remuneración ordinaria + pago por trabajo en feriado)
         * - Domingo trabajado: básico diario x2 (remuneración ordinaria + sobretasa 100% por domingo)
         * - Feriado que cae en domingo y se trabaja: básico diario x3 (pago triple)
         *   * Remuneración ordinaria: x1 (día de descanso remunerado del feriado)
         *   * Pago por trabajo en feriado: x1 adicional (total x2)
         *   * Sobretasa 100% por trabajar en domingo: x1 adicional (total x3)
         * 
         * Fórmula: Suma de (básico_diario * multiplicador) para cada día trabajado
         * 
         * El salario mensual viene del contexto y puede ser:
         * - Salario especial de contract_position_salaries (si existe)
         * - Salario de la posición (positions.salary)
         */

        BigDecimal monthlySalary = context.getBasicSalary();

        if (monthlySalary == null || monthlySalary.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("No basic salary configured for employee {}", context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }

        // Dividir el salario mensual entre 30 para obtener el básico diario
        BigDecimal dailyBasicSalary = monthlySalary.divide(
            new BigDecimal("30"), 
            4, 
            RoundingMode.HALF_UP
        );
        
        // Calcular día por día con multiplicadores
        BigDecimal totalBasicSalary = BigDecimal.ZERO;
        Map<java.time.LocalDate, WorkCalendarDayInfo> calendarInfo = context.getCalendarInfo();
        Set<java.time.LocalDate> workedDays = context.getWorkedDays();
        
        if (workedDays == null || workedDays.isEmpty()) {
            log.warn("No worked days for employee {}", context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }
        
        for (java.time.LocalDate date : workedDays) {
            WorkCalendarDayInfo dayInfo = calendarInfo != null ? calendarInfo.get(date) : null;
            
            // Determinar multiplicador según el tipo de día
            BigDecimal multiplier = BigDecimal.ONE; // Día normal
            
            if (dayInfo != null) {
                boolean isHoliday = dayInfo.isHoliday();
                boolean isSunday = dayInfo.isSunday();
                
                if (isHoliday && isSunday) {
                    // Feriado que cae en domingo y se trabaja: x3 (pago triple)
                    // Remuneración ordinaria (x1) + Pago por trabajo en feriado (x1) + Sobretasa 100% domingo (x1) = x3
                    multiplier = new BigDecimal("3");
                    log.debug("Feriado en domingo trabajado {}: básico x3 (pago triple)", date);
                } else if (isHoliday) {
                    // Día feriado trabajado: x2 (remuneración ordinaria + pago por trabajo en feriado)
                    multiplier = new BigDecimal("2");
                    log.debug("Día feriado trabajado {}: básico x2", date);
                } else if (isSunday) {
                    // Domingo trabajado: x2 (remuneración ordinaria + sobretasa 100% por domingo)
                    multiplier = new BigDecimal("2");
                    log.debug("Domingo trabajado {}: básico x2", date);
                }
            } else {
                // Si no hay información del calendario, verificar si es domingo
                if (date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                    multiplier = new BigDecimal("2");
                    log.debug("Domingo trabajado {} (sin info calendario): básico x2", date);
                }
            }
            
            // Calcular básico para este día: básico_diario * multiplicador
            BigDecimal dayBasicSalary = dailyBasicSalary.multiply(multiplier);
            totalBasicSalary = totalBasicSalary.add(dayBasicSalary);
        }
        
        return totalBasicSalary.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public ConceptCode getConceptCode() {
        return ConceptCode.BASIC_SALARY;
    }
}