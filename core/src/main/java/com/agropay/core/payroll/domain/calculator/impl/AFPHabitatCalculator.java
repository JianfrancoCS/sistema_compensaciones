package com.agropay.core.payroll.domain.calculator.impl;

import com.agropay.core.payroll.domain.enums.ConceptCode;
import org.springframework.stereotype.Component;

/**
 * Calculator for AFP Habitat
 */
@Component
public class AFPHabitatCalculator extends AbstractAFPCalculator {

    @Override
    public ConceptCode getConceptCode() {
        return ConceptCode.AFP_HABITAT;
    }
}