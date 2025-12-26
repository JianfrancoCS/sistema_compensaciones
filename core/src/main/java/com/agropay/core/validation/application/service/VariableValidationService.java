package com.agropay.core.validation.application.service;

import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.validation.domain.DynamicVariableMethodEntity;
import com.agropay.core.validation.domain.ValidationMethodEntity;
import com.agropay.core.validation.exceptions.ValidationMethodException;
import com.agropay.core.validation.persistence.IValidationMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VariableValidationService {

    private final IValidationMethodRepository validationMethodRepository;

    public void validateMethodsExist(List<UUID> methodPublicIds) {
        List<ValidationMethodEntity> existingMethods = validationMethodRepository.findByPublicIdIn(methodPublicIds);

        if (existingMethods.size() != methodPublicIds.size()) {
            throw new ValidationMethodException("exception.validation.methods.some-not-found");
        }
    }

    public void validateRequiredValues(List<MethodValidationRequest> methods) {
        for (MethodValidationRequest methodRequest : methods) {
            ValidationMethodEntity method = validationMethodRepository.findByPublicId(methodRequest.getMethodPublicId())
                .orElseThrow(() -> new IdentifierNotFoundException("exception.validation.method.not-found", methodRequest.getMethodPublicId().toString()));

            if (method.getRequiresValue()) {
                String value = methodRequest.getValue();
                if (value == null || value.trim().isEmpty()) {
                    throw new ValidationMethodException("exception.validation.method.value-required", method.getName());
                }

                validateValueFormat(method, value.trim());
            }
        }
    }

    public void validateExecutionOrder(List<MethodValidationRequest> methods) {
        List<Integer> orders = methods.stream().map(MethodValidationRequest::getExecutionOrder).toList();
        boolean hasDuplicates = orders.size() != orders.stream().distinct().count();

        if (hasDuplicates) {
            throw new ValidationMethodException("exception.validation.method.duplicate-execution-order", "");
        }
    }

    public String generateFinalRegex(List<DynamicVariableMethodEntity> variableMethods) {
        List<DynamicVariableMethodEntity> orderedMethods = variableMethods.stream()
            .sorted(Comparator.comparing(DynamicVariableMethodEntity::getExecutionOrder))
            .toList();

        List<String> lookAheads = new ArrayList<>();
        String characterClass = null;
        String quantifier = null;

        for (DynamicVariableMethodEntity vm : orderedMethods) {
            ValidationMethodEntity method = vm.getValidationMethod();

            switch (method.getMethodType()) {
                case REGEX:
                    String pattern = method.getRegexPattern();
                    if (pattern != null) {
                        if (pattern.startsWith("(?=")) {
                            lookAheads.add(pattern);
                        } else {
                            pattern = pattern.replaceAll("^\\^|\\$$", "");
                            if (pattern.matches("\\[.*\\]\\+?") || pattern.matches("\\\\[dDwWsS]\\+?")) {
                                characterClass = pattern.replaceAll("\\+$", "");
                            } else {
                                characterClass = pattern.replaceAll("[+*?]$", "");
                            }
                        }
                    }
                    break;

                case LENGTH:
                    quantifier = processLengthQuantifier(method, vm.getValue());
                    break;

                case COMPARISON:
                    log.debug("Skipping comparison method {} - will be validated separately", method.getCode());
                    break;
            }
        }

        StringBuilder finalRegex = new StringBuilder("^");
        finalRegex.append(String.join("", lookAheads));

        if (characterClass != null) {
            finalRegex.append(characterClass);
            if (quantifier != null) {
                finalRegex.append(quantifier);
            } else {
                if (!characterClass.endsWith("+") && !characterClass.endsWith("*") && !characterClass.endsWith("}")) {
                    finalRegex.append("+");
                }
            }
        } else if (quantifier != null) {
            finalRegex.append(".").append(quantifier);
        } else {
            finalRegex.append(".*");
        }

        finalRegex.append("$");
        return finalRegex.toString();
    }

    private void validateValueFormat(ValidationMethodEntity method, String trimmedValue) {
        switch (method.getCode()) {
            case "EXACT_LENGTH":
            case "MIN_LENGTH":
                if (!trimmedValue.matches("\\d+")) {
                    throw new ValidationMethodException("exception.validation.method.invalid-numeric-value", method.getName());
                }
                break;

            case "LENGTH_RANGE":
                if (!trimmedValue.matches("\\d+,\\d+")) {
                    throw new ValidationMethodException("exception.validation.method.invalid-range-value", method.getName());
                }
                String[] parts = trimmedValue.split(",");
                int min = Integer.parseInt(parts[0].trim());
                int max = Integer.parseInt(parts[1].trim());
                if (min >= max) {
                    throw new ValidationMethodException("exception.validation.method.invalid-range-order", method.getName());
                }
                break;

            case "GREATER_THAN":
            case "LESS_THAN":
                if (!trimmedValue.matches("-?\\d+(\\.\\d+)?")) {
                    throw new ValidationMethodException("exception.validation.method.invalid-numeric-value", method.getName());
                }
                break;
        }
    }

    private String processLengthQuantifier(ValidationMethodEntity method, String value) {
        if (value == null) {
            return null;
        }

        String methodCode = method.getCode();
        String trimmedValue = value.trim();

        switch (methodCode) {
            case "EXACT_LENGTH":
                return "{" + trimmedValue + "}";

            case "MIN_LENGTH":
                return "{" + trimmedValue + ",}";

            case "LENGTH_RANGE":
                String[] parts = trimmedValue.split(",");
                if (parts.length == 2) {
                    return "{" + parts[0].trim() + "," + parts[1].trim() + "}";
                }
                break;
        }

        return null;
    }

    // Interface común para validación de métodos
    public interface MethodValidationRequest {
        UUID getMethodPublicId();
        String getValue();
        Integer getExecutionOrder();
    }
}