package com.agropay.core.payroll.domain.calculator.impl;

import com.agropay.core.payroll.domain.calculator.ConceptCalculator;
import com.agropay.core.payroll.domain.calculator.EmployeePayrollContext;
import com.agropay.core.payroll.domain.calculator.PieceworkDayInfo;
import com.agropay.core.payroll.domain.enums.ConceptCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

/**
 * Calculator for piecework excess payment (destajo excedente)
 * 
 * IMPORTANTE: Este concepto SOLO aplica para empleados con labores de DESTAJO
 * 
 * El pago del excedente se calcula día por día:
 * - Si en un día el empleado supera el mínimo de productividad, se calcula el excedente
 * - Excedente = (productividad_del_día - min_task_requirement)
 * - Pago del excedente = excedente * base_price de la labor
 * 
 * Ejemplo:
 * - Labor: Cosecha de Uva
 * - Min Task Requirement: 25 jabas
 * - Base Price: 1.50 soles por jaba
 * - Productividad del día: 30 jabas
 * - Excedente: 30 - 25 = 5 jabas
 * - Pago del excedente: 5 * 1.50 = 7.50 soles
 */
@Slf4j
@Component
public class PieceworkExcessCalculator implements ConceptCalculator {

    @Override
    public BigDecimal calculate(EmployeePayrollContext context) {
        /**
         * Calcula el pago del excedente de destajo día por día
         *
         * REGLAS:
         * 1. El pago SOLO aplica para empleados con labores de DESTAJO
         * 2. Solo se paga el excedente (productividad - mínimo requerido)
         * 3. El pago se calcula: excedente * base_price de la labor
         * 4. Si no hay excedente (productividad <= mínimo), no se paga nada
         *
         * Fórmula: Suma de (excedente * base_price) por cada día con excedente
         */

        Map<LocalDate, PieceworkDayInfo> pieceworkInfoPerDay = context.getPieceworkInfoPerDay();

        // Si pieceworkInfoPerDay es null o vacío, significa que el empleado NO tiene labores de destajo
        // (es administrativo), por lo tanto NO aplica el pago del excedente
        if (pieceworkInfoPerDay == null || pieceworkInfoPerDay.isEmpty()) {
            log.debug("Empleado {} no tiene información de destajo (probablemente labores administrativas)", 
                context.getEmployeeDocumentNumber());
            return BigDecimal.ZERO;
        }

        BigDecimal totalExcessPayment = BigDecimal.ZERO;

        for (Map.Entry<LocalDate, PieceworkDayInfo> entry : pieceworkInfoPerDay.entrySet()) {
            LocalDate date = entry.getKey();
            PieceworkDayInfo dayInfo = entry.getValue();

            if (dayInfo == null) {
                continue;
            }

            // Calcular el pago del excedente para este día
            BigDecimal excessPayment = dayInfo.calculateExcessPayment();

            if (excessPayment.compareTo(BigDecimal.ZERO) > 0) {
                totalExcessPayment = totalExcessPayment.add(excessPayment);
                log.debug("Empleado {} - Día {}: Excedente de {} unidades, pago: {} soles", 
                    context.getEmployeeDocumentNumber(), 
                    date,
                    dayInfo.calculateExcess(),
                    excessPayment);
            } else {
                log.debug("Empleado {} - Día {}: No hay excedente (productividad: {}, mínimo: {})", 
                    context.getEmployeeDocumentNumber(), 
                    date,
                    dayInfo.productivityCount(),
                    dayInfo.minTaskRequirement());
            }
        }

        log.debug("Empleado {}: Pago total de excedente de destajo: {} soles", 
            context.getEmployeeDocumentNumber(), totalExcessPayment);

        return totalExcessPayment.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public ConceptCode getConceptCode() {
        return ConceptCode.PIECEWORK_EXCESS;
    }
}

