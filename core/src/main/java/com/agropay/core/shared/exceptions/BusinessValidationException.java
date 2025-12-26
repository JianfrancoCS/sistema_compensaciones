package com.agropay.core.shared.exceptions;

public class BusinessValidationException extends GenericException {
    public BusinessValidationException(String message, Object... args) {
        super(message, args);
    }
}