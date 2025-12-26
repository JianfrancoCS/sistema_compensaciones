package com.agropay.core.payroll.domain.calculator.impl;

import com.agropay.core.payroll.domain.WorkCalendarEntity;
import com.agropay.core.payroll.domain.calculator.ConceptCalculator;
import com.agropay.core.payroll.domain.calculator.EmployeePayrollContext;
import com.agropay.core.payroll.domain.enums.ConceptCode;
import com.agropay.core.payroll.persistence.IWorkCalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculator for dominical payment
 * 
 * El dominical es un jornal (1 día de básico diario) con beneficios de bonos.
 * 
 * REGLAS:
 * 1. El dominical se otorga si el empleado trabajó todos los días laborales de lunes a sábado
 *    según el calendario de la empresa, sin faltar ningún día.
 * 2. El domingo NO cuenta para el cumplimiento de la semana completa.
 * 3. Si la empresa marca un día de lunes a sábado como no laboral, ese día no cuenta.
 * 4. Si el empleado faltó algún día laboral de lunes a sábado, no tiene derecho al dominical de esa semana.
 * 
 * EJEMPLO:
 * - Calendario empresa: lunes a domingo son días laborales
 * - Empleado trabajó: lunes a sábado (sin faltar)
 * - Resultado: Tiene derecho al dominical (aunque no trabajó el domingo)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DominicalCalculator implements ConceptCalculator {

    private final IWorkCalendarRepository workCalendarRepository;

    @Override
    public BigDecimal calculate(EmployeePayrollContext context) {
        /**
         * Calcula el pago dominical semana por semana.
         * 
         * Reglas:
         * 1. Se agrupa el período por semanas (lunes a domingo)
         * 2. Para cada semana, se verifica:
         *    - Días laborales de lunes a sábado según calendario de la empresa
         *    - Días trabajados por el empleado (con tareos)
         * 3. Si el empleado trabajó todos los días laborales de lunes a sábado → se otorga el dominical
         * 4. Si faltó algún día laboral de lunes a sábado → no se otorga el dominical de esa semana
         * 5. El domingo NO cuenta para el cumplimiento de la semana completa
         * 
         * El monto del dominical está configurado en PayrollConfiguration.
         * Si no está configurado, se calcula como 1 día de básico diario.
         */

        // El dominical SIEMPRE es 1 día de básico diario (salario mensual / 30)
        // No se usa valor configurado, siempre se calcula dinámicamente
        BigDecimal monthlySalary = context.getBasicSalary();
        if (monthlySalary == null || monthlySalary.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("No basic salary configured for dominical calculation for employee {}", 
                context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }
        
        // Calcular básico diario: salario mensual / 30
        // Este es el monto del dominical por semana completa
        BigDecimal dominicalAmount = monthlySalary.divide(new BigDecimal("30"), 4, RoundingMode.HALF_UP);

        LocalDate periodStart = context.getPeriodStart();
        LocalDate periodEnd = context.getPeriodEnd();
        Set<LocalDate> workedDays = context.getWorkedDays();
        
        if (periodStart == null || periodEnd == null || workedDays == null || workedDays.isEmpty()) {
            log.warn("Missing period information or no worked days for dominical calculation for employee {}", 
                context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }

        // Obtener todos los días del calendario en el período (incluyendo no laborales)
        List<WorkCalendarEntity> calendarDays = workCalendarRepository.findAllBetweenWithEvents(
            periodStart, periodEnd
        );

        // Agrupar por semanas y verificar cumplimiento
        // El dominical se otorga SOLO por semana completa (sin faltar días laborales)
        int completedWeeks = countCompletedWeeks(periodStart, periodEnd, calendarDays, workedDays);
        
        // Calcular total: dominical por cada semana completa
        // NOTA: Si el empleado trabajó el domingo, ese día se paga doble (básico x2) en BasicSalaryCalculator,
        // pero el dominical se otorga SOLO si cumplió la semana completa (sin faltar días laborales)
        BigDecimal totalDominical = dominicalAmount.multiply(new BigDecimal(completedWeeks));
        
        log.info("Empleado {}: Salario mensual: {}, Básico diario: {}, Semanas completas: {}, Dominical por semana: {}, Dominical total: {}", 
            context.getEmployeeDocumentNumber(), 
            monthlySalary,
            dominicalAmount,
            completedWeeks, 
            dominicalAmount,
            totalDominical);
        
        return totalDominical.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Cuenta las semanas completas en el período.
     * Una semana se considera completa si el empleado trabajó todos los días laborales de esa semana.
     */
    private int countCompletedWeeks(
            LocalDate periodStart, 
            LocalDate periodEnd, 
            List<WorkCalendarEntity> calendarDays,
            Set<LocalDate> workedDays) {
        
        // Crear mapa de días laborales por fecha
        Map<LocalDate, Boolean> isWorkingDayMap = calendarDays.stream()
            .collect(Collectors.toMap(
                WorkCalendarEntity::getDate,
                WorkCalendarEntity::isWorkingDay,
                (v1, v2) -> v1
            ));

        int completedWeeks = 0;
        LocalDate currentWeekStart = getWeekStart(periodStart);
        
        while (!currentWeekStart.isAfter(periodEnd)) {
            LocalDate weekEnd = currentWeekStart.plusDays(6); // Domingo de la semana
            
            // Ajustar si la semana se extiende más allá del período
            if (weekEnd.isAfter(periodEnd)) {
                weekEnd = periodEnd;
            }
            
            // Verificar si esta semana está completa
            boolean isComplete = isWeekComplete(currentWeekStart, weekEnd, isWorkingDayMap, workedDays);
            if (isComplete) {
                completedWeeks++;
                log.debug("Semana completa encontrada: {} a {}", currentWeekStart, weekEnd);
            } else {
                log.debug("Semana incompleta: {} a {} (faltó algún día laboral de lunes a sábado)", currentWeekStart, weekEnd);
            }
            
            // Avanzar a la siguiente semana (lunes)
            currentWeekStart = currentWeekStart.plusWeeks(1);
        }
        
        return completedWeeks;
    }

    /**
     * Obtiene el lunes de la semana que contiene la fecha dada
     */
    private LocalDate getWeekStart(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int daysToSubtract = dayOfWeek.getValue() - DayOfWeek.MONDAY.getValue();
        if (daysToSubtract < 0) {
            daysToSubtract += 7; // Si es domingo, retroceder 6 días
        }
        return date.minusDays(daysToSubtract);
    }

    /**
     * Verifica si una semana está completa (empleado trabajó todos los días laborales de lunes a sábado).
     * 
     * IMPORTANTE: El dominical se otorga si el empleado trabajó todos los días laborales de lunes a sábado,
     * independientemente de si el domingo se trabajó o no. El domingo no cuenta para el cumplimiento de la semana.
     * 
     * Ejemplo:
     * - Calendario empresa: lunes a domingo son días laborales
     * - Empleado trabajó: lunes a sábado (sin faltar)
     * - Resultado: Tiene derecho al dominical (aunque no trabajó el domingo)
     */
    private boolean isWeekComplete(
            LocalDate weekStart,
            LocalDate weekEnd,
            Map<LocalDate, Boolean> isWorkingDayMap,
            Set<LocalDate> workedDays) {
        
        LocalDate currentDate = weekStart; // Lunes
        LocalDate saturday = weekStart.plusDays(5); // Sábado
        
        // Solo verificar de lunes a sábado (el domingo no cuenta para el cumplimiento)
        while (!currentDate.isAfter(saturday) && !currentDate.isAfter(weekEnd)) {
            // Verificar si es día laboral según el calendario de la empresa
            Boolean isWorkingDay = isWorkingDayMap.get(currentDate);
            
            // Si no está en el calendario, asumir que es laboral (excepto domingos)
            if (isWorkingDay == null) {
                isWorkingDay = currentDate.getDayOfWeek() != DayOfWeek.SUNDAY;
            }
            
            // Si es día laboral (lunes a sábado), debe estar trabajado
            if (Boolean.TRUE.equals(isWorkingDay)) {
                if (!workedDays.contains(currentDate)) {
                    // Faltó un día laboral de lunes a sábado → semana incompleta
                    return false;
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        // Todos los días laborales de lunes a sábado fueron trabajados → semana completa
        return true;
    }

    @Override
    public ConceptCode getConceptCode() {
        return ConceptCode.DOMINICAL;
    }
}

