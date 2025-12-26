package com.agropay.core.shared.exceptions;

public abstract class GenericException extends RuntimeException {
    private final Object[] parameters;

    protected GenericException(String message) {
        this(message, (Object[]) null);
    }

    protected GenericException(String message, Object... parameters) {
        super(message);
        this.parameters = parameters != null ? parameters.clone() : new Object[0];
    }

    protected GenericException(String message, Throwable cause, Object... parameters) {
        super(message, cause);
        this.parameters = parameters != null ? parameters.clone() : new Object[0];
    }

    public Object[] getParameters() {
        return parameters.length > 0 ? parameters.clone() : parameters;
    }
}