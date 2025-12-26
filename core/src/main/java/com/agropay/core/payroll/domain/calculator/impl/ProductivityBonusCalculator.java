package com.agropay.core.payroll.domain.calculator.impl;

import com.agropay.core.payroll.domain.calculator.ConceptCalculator;
import com.agropay.core.payroll.domain.calculator.EmployeePayrollContext;
import com.agropay.core.payroll.domain.enums.ConceptCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Calculator for productivity bonus
 * 
 * IMPORTANTE: Este bono SOLO aplica para empleados con labores de DESTAJO
 * 
 * El bono se calcula día por día:
 * - Si en un día el empleado supera el mínimo de productividad (>= 100%), se activa el bono para ese día
 * - El bono configurado es el monto por día (ej: 3 soles por día)
 * - Se suma el bono por cada día que se cumplió el mínimo
 */
@Slf4j
@Component
public class ProductivityBonusCalculator implements ConceptCalculator {

    @Override
    public BigDecimal calculate(EmployeePayrollContext context) {
        /**
         * Calcula el bono por productividad día por día
         *
         * REGLAS:
         * 1. El bono SOLO aplica para empleados con labores de DESTAJO
         * 2. Cada labor de destajo tiene un minTaskRequirement (mínimo de tareas requeridas)
         * 3. Si en un día el empleado supera el mínimo (productividad >= 100%), se activa el bono para ese día
         * 4. El bono configurado es el monto por día (ej: 3 soles por día)
         * 5. Se suma el bono por cada día que se cumplió el mínimo
         *
         * Fórmula: Suma de (bonusAmount) por cada día con productividad >= 100%
         */

        // Obtener el monto del bono por día desde la configuración de planilla
        // Este valor se configura en tbl_payroll_configuration_concepts y representa el monto por día
        // (ej: 3.00 soles por día)
        BigDecimal bonusAmountPerDay = context.getConfiguredConcepts().get(ConceptCode.PRODUCTIVITY_BONUS);

        if (bonusAmountPerDay == null) {
            log.debug("Bono de productividad no configurado en la planilla para empleado {}", 
                context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }

        Map<java.time.LocalDate, BigDecimal> productivityPerDay = context.getProductivityPerDay();

        // Si productivityPerDay es null o vacío, significa que el empleado NO tiene labores de destajo
        // (es administrativo), por lo tanto NO aplica el bono de productividad
        if (productivityPerDay == null || productivityPerDay.isEmpty()) {
            log.debug("Empleado {} no tiene productividad por día (probablemente labores administrativas)", 
                context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }

        // Contar días con productividad >= 100% (cumplió el mínimo)
        int daysWithBonus = 0;
        BigDecimal minProductivity = new BigDecimal("100");
        
        for (Map.Entry<java.time.LocalDate, BigDecimal> entry : productivityPerDay.entrySet()) {
            BigDecimal productivity = entry.getValue();
            
            // Si la productividad es >= 100%, se activa el bono para ese día
            if (productivity != null && productivity.compareTo(minProductivity) >= 0) {
                daysWithBonus++;
                log.debug("Empleado {} cumplió productividad en fecha {}: {}% → bono activado", 
                    context.getEmployeeDocumentNumber(), entry.getKey(), productivity);
            } else {
                log.debug("Empleado {} NO cumplió productividad en fecha {}: {}% → sin bono", 
                    context.getEmployeeDocumentNumber(), entry.getKey(), 
                    productivity != null ? productivity : "null");
            }
        }

        // Calcular total: bono por día * días con productividad >= 100%
        BigDecimal totalBonus = bonusAmountPerDay.multiply(new BigDecimal(daysWithBonus));
        
        log.debug("Empleado {}: {} días con bono de productividad, total: {}", 
            context.getEmployeeDocumentNumber(), daysWithBonus, totalBonus);
        
        return totalBonus.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public ConceptCode getConceptCode() {
        return ConceptCode.PRODUCTIVITY_BONUS;
    }
}