package com.agropay.core.shared.exceptions;

public class UniqueValidationException extends GenericException {
    public UniqueValidationException(String message, Object... args) {
        super(message, args);
    }
}
