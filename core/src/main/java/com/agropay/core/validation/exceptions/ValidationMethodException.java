package com.agropay.core.validation.exceptions;

import com.agropay.core.shared.exceptions.GenericException;

public class ValidationMethodException extends GenericException {
    public ValidationMethodException(String message) {
        super(message);
    }

    public ValidationMethodException(String message, Object... args) {
        super(message, args);
    }
}