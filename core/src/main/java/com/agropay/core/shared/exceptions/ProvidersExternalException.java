package com.agropay.core.shared.exceptions;

public class ProvidersExternalException extends GenericException {
    public ProvidersExternalException(String message) {
        super(message);
    }
    public ProvidersExternalException(String message, Object... args) {
        super(message, args);
    }
}
