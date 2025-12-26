package com.agropay.core.validation.exceptions;

import com.agropay.core.shared.exceptions.GenericException;

public class DynamicVariableException extends GenericException {
    public DynamicVariableException(String message) {
        super(message);
    }

    public DynamicVariableException(String message, Object... args) {
        super(message, args);
    }
}