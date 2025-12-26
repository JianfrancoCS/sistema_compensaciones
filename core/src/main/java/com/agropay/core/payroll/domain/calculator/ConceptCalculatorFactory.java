package com.agropay.core.payroll.domain.calculator;

import com.agropay.core.payroll.domain.enums.ConceptCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for retrieving the appropriate calculator for a concept code
 */
@Slf4j
@Component
public class ConceptCalculatorFactory {

    private final Map<ConceptCode, ConceptCalculator> calculators;

    /**
     * Constructor that auto-registers all ConceptCalculator beans
     *
     * @param calculatorList List of all ConceptCalculator beans injected by Spring
     */
    public ConceptCalculatorFactory(List<ConceptCalculator> calculatorList) {
        this.calculators = calculatorList.stream()
            .collect(Collectors.toMap(
                ConceptCalculator::getConceptCode,
                Function.identity()
            ));

        log.info("Registered {} concept calculators: {}",
            calculators.size(),
            calculators.keySet());
    }

    /**
     * Get the calculator for a specific concept code
     *
     * @param conceptCode The concept code
     * @return The calculator for this concept
     * @throws IllegalArgumentException if no calculator is registered for the code
     */
    public ConceptCalculator getCalculator(ConceptCode conceptCode) {
        ConceptCalculator calculator = calculators.get(conceptCode);

        if (calculator == null) {
            throw new IllegalArgumentException(
                "No calculator registered for concept code: " + conceptCode
            );
        }

        return calculator;
    }

    /**
     * Check if a calculator exists for a concept code
     *
     * @param conceptCode The concept code
     * @return true if a calculator is registered
     */
    public boolean hasCalculator(ConceptCode conceptCode) {
        return calculators.containsKey(conceptCode);
    }
}
