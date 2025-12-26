package com.agropay.core.payroll.domain.calculator.impl;

import com.agropay.core.payroll.domain.calculator.ConceptCalculator;
import com.agropay.core.payroll.domain.calculator.EmployeePayrollContext;
import com.agropay.core.payroll.domain.enums.ConceptCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculator for family allowance
 */
@Slf4j
@Component
public class FamilyAllowanceCalculator implements ConceptCalculator {

    @Override
    public BigDecimal calculate(EmployeePayrollContext context) {
        /**
         * Calcula la asignación familiar
         *
         * En Perú, la asignación familiar es el 10% de la RMV (Remuneración Mínima Vital)
         * Se otorga a trabajadores con hijos menores de edad o con discapacidad
         *
         * Condiciones:
         * - Tener al menos 1 dependiente (hijo menor de edad)
         * - No hay límite de dependientes (se paga monto fijo, no por hijo)
         *
         * Los dependientes se cuentan desde tbl_persons.person_parent_document_number
         *
         * Fórmula: Monto fijo si tiene >= 1 dependiente, 0 si no tiene
         */

        Integer numberOfDependents = context.getNumberOfDependents();

        if (numberOfDependents == null || numberOfDependents == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal allowanceAmount = context.getConfiguredConcepts().get(ConceptCode.FAMILY_ALLOWANCE);

        if (allowanceAmount == null) {
            return BigDecimal.ZERO;
        }

        // Retornar monto fijo si tiene al menos un dependiente
        return allowanceAmount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public ConceptCode getConceptCode() {
        return ConceptCode.FAMILY_ALLOWANCE;
    }
}