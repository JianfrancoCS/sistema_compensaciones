package com.agropay.core.shared.exceptions;

public class DataIntegrityViolationException extends GenericException {
    public DataIntegrityViolationException(String message, Object... args) {
        super(message, args);
    }
}
