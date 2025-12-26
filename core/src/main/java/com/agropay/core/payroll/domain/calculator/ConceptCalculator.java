package com.agropay.core.payroll.domain.calculator;

import com.agropay.core.payroll.domain.enums.ConceptCode;

import java.math.BigDecimal;

/**
 * Strategy interface for calculating payroll concepts
 */
public interface ConceptCalculator {

    /**
     * Calculate the concept amount for the given employee context
     *
     * @param context The employee payroll context with all necessary data
     * @return The calculated amount
     */
    BigDecimal calculate(EmployeePayrollContext context);

    /**
     * Get the concept code this calculator handles
     *
     * @return The concept code
     */
    ConceptCode getConceptCode();
}