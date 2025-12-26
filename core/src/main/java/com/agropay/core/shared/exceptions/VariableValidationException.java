package com.agropay.core.shared.exceptions;

public class VariableValidationException extends GenericException {
    public VariableValidationException(String message) {
        super(message);
    }

    public VariableValidationException(String message, Object... args) {
        super(message, args);
    }
}