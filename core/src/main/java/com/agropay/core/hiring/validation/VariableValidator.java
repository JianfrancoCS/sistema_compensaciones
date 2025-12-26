package com.agropay.core.hiring.validation;

import com.agropay.core.hiring.domain.ContractTemplateEntity;
import com.agropay.core.hiring.domain.ContractTemplateVariableEntity;
import com.agropay.core.hiring.domain.VariableEntity;
import com.agropay.core.hiring.model.contracttemplate.ContractTemplateVariableWithValidationDTO;
import com.agropay.core.hiring.persistence.IContractTemplateVariableRepository;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.validation.domain.DynamicVariableEntity;
import com.agropay.core.validation.domain.DynamicVariableMethodEntity;
import com.agropay.core.validation.domain.ValidationMethodEntity;
import com.agropay.core.validation.persistence.IDynamicVariableMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
@RequiredArgsConstructor
@Slf4j
public class VariableValidator {

    private final IContractTemplateVariableRepository contractTemplateVariableRepository;
    private final IDynamicVariableMethodRepository dynamicVariableMethodRepository;

    public void validateVariablesForTemplate(ContractTemplateEntity template, Map<String, String> variableValues) {
        log.debug("Validating variables for template: {}", template.getPublicId());

        List<ContractTemplateVariableEntity> templateVariables = contractTemplateVariableRepository
                .findByContractTemplateOrderByDisplayOrder(template);

        List<String> validationErrors = new ArrayList<>();

        for (ContractTemplateVariableEntity templateVar : templateVariables) {
            VariableEntity variable = templateVar.getVariable();
            String variableCode = variable.getCode();
            String value = variableValues.get(variableCode);

            if (value != null && variable.getDynamicVariable() != null) {
                String validationError = validateSingleVariable(variable, value);
                if (validationError != null) {
                    validationErrors.add(String.format("%s: %s", variableCode, validationError));
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            String allErrors = String.join(", ", validationErrors);
            throw new BusinessValidationException("exception.shared.variable-validation-failed", allErrors);
        }

        log.debug("All variables validated successfully for template: {}", template.getPublicId());
    }

    public String validateSingleVariable(VariableEntity variable, String value) {
        DynamicVariableEntity dynamicVariable = variable.getDynamicVariable();

        if (dynamicVariable == null) {
            return null;
        }

        String finalRegex = dynamicVariable.getFinalRegex();
        String customErrorMessage = dynamicVariable.getErrorMessage();

        if (finalRegex == null || finalRegex.trim().isEmpty()) {
            log.warn("Variable {} has dynamic validation but no final regex", variable.getCode());
            return null;
        }

        try {
            // 1. Validar regex (formato)
            Pattern pattern = Pattern.compile(finalRegex);
            boolean matches = pattern.matcher(value).matches();

            if (!matches) {
                log.debug("Variable {} failed regex validation. Value: '{}', Regex: '{}'",
                         variable.getCode(), value, finalRegex);
                return customErrorMessage != null ? customErrorMessage : "El formato no es válido";
            }

            // 2. Validar comparaciones numéricas (si las hay)
            String comparisonError = validateComparisonMethods(dynamicVariable, value);
            if (comparisonError != null) {
                return comparisonError;
            }

            return null;

        } catch (PatternSyntaxException e) {
            log.error("Invalid regex pattern for variable {}: {}", variable.getCode(), finalRegex, e);
            return "Error interno de validación";
        }
    }

    public void validateVariablesWithDetails(List<ContractTemplateVariableWithValidationDTO> variablesWithValidation,
                                           Map<String, String> variableValues) {
        log.debug("Validating {} variables with detailed validation info", variablesWithValidation.size());

        List<String> validationErrors = new ArrayList<>();

        for (ContractTemplateVariableWithValidationDTO varInfo : variablesWithValidation) {
            String variableCode = varInfo.code();
            String value = variableValues.get(variableCode);

            if (value != null && varInfo.validation().hasValidation()) {
                String finalRegex = varInfo.validation().finalRegex();
                String customErrorMessage = varInfo.validation().errorMessage();

                if (finalRegex != null && !finalRegex.trim().isEmpty()) {
                    try {
                        Pattern pattern = Pattern.compile(finalRegex);
                        boolean matches = pattern.matcher(value).matches();

                        if (!matches) {
                            String errorMsg = customErrorMessage != null ? customErrorMessage : "El formato no es válido";
                            validationErrors.add(String.format("%s: %s", variableCode, errorMsg));
                        }

                    } catch (PatternSyntaxException e) {
                        log.error("Invalid regex pattern for variable {}: {}", variableCode, finalRegex, e);
                        validationErrors.add(String.format("%s: Error interno de validación", variableCode));
                    }
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            String allErrors = String.join(", ", validationErrors);
            throw new BusinessValidationException("exception.shared.variable-validation-failed", allErrors);
        }

        log.debug("All variables with detailed validation passed successfully");
    }

    private String validateComparisonMethods(DynamicVariableEntity dynamicVariable, String value) {
        List<DynamicVariableMethodEntity> comparisonMethods = dynamicVariableMethodRepository
                .findByDynamicVariableOrderByExecutionOrder(dynamicVariable)
                .stream()
                .filter(method -> method.getValidationMethod().getMethodType().name().equals("COMPARISON"))
                .toList();

        if (comparisonMethods.isEmpty()) {
            return null;
        }

        // Intentar convertir el valor a número para comparaciones
        Double numericValue;
        try {
            numericValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.debug("Value '{}' is not numeric, skipping comparison validations", value);
            return null; // Si no es numérico, no se pueden hacer comparaciones
        }

        for (DynamicVariableMethodEntity method : comparisonMethods) {
            ValidationMethodEntity validationMethod = method.getValidationMethod();
            String methodValue = method.getValue();

            if (methodValue == null || methodValue.trim().isEmpty()) {
                continue; // Saltar métodos sin valor
            }

            try {
                Double compareValue = Double.parseDouble(methodValue.trim());
                String error = performComparison(validationMethod.getCode(), numericValue, compareValue);
                if (error != null) {
                    return error;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid comparison value '{}' for method {}", methodValue, validationMethod.getCode());
            }
        }

        return null;
    }

    private String performComparison(String methodCode, Double value, Double compareValue) {
        switch (methodCode) {
            case "GREATER_THAN":
                if (value <= compareValue) {
                    return String.format("El valor debe ser mayor que %s", compareValue);
                }
                break;
            case "LESS_THAN":
                if (value >= compareValue) {
                    return String.format("El valor debe ser menor que %s", compareValue);
                }
                break;
            case "GREATER_THAN_OR_EQUAL":
                if (value < compareValue) {
                    return String.format("El valor debe ser mayor o igual que %s", compareValue);
                }
                break;
            case "LESS_THAN_OR_EQUAL":
                if (value > compareValue) {
                    return String.format("El valor debe ser menor o igual que %s", compareValue);
                }
                break;
        }
        return null;
    }
}